package com.lucab.shadows_things.content.block.resonant.resonant_altar;

import com.lucab.shadows_things.content.block.resonant.ResonantHelper;
import com.lucab.shadows_things.content.block.resonant.resonant_pedestal.ResonantPedestalBlockEntity;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.lucab.shadows_things.recipe.ResonantAltarRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResonantAltarBlockEntity extends BlockEntity {
    public final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final List<ResonantPedestalBlockEntity> pedestals = new ArrayList<>();
    private boolean structureDirty = true;

    private int processTime = 0;
    private int totalProcessTime = 0;
    private ResonantAltarRecipe activeRecipe = null;

    public ResonantAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ResonantAltarRegistry.RESONANT_ALTAR_ENTITY.get(), pos, state);
    }

    // ==========================================
    // INVENTORY MANAGEMENT
    // ==========================================

    public boolean insertItem(ItemStack stack) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (insertItem(stack, i).isEmpty()) return true;
        }
        return false;
    }

    public ItemStack insertItem(ItemStack stack, int slot) {
        if (!hasItem(slot)) {
            if (inventory.isItemValid(slot, stack)) {
                return inventory.insertItem(slot, stack.copyWithCount(1), false);
            }
        }
        return stack;
    }

    public ItemStack removeItems() {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack removedItem = removeItem(i);
            if (!removedItem.isEmpty()) return removedItem;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack removeItem(int slot) {
        return inventory.extractItem(slot, 1, false);
    }

    public ItemStack removeTool() {
        return removeItem(0);
    }

    public void removeReagents() {
        for (int i = 1; i < inventory.getSlots(); i++) {
            removeItem(i);
        }
    }

    public void removeOfferings() {
        for (ResonantPedestalBlockEntity pedestal : pedestals) {
            pedestal.removeItem();
        }
    }

    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    public ItemStack getTool() {
        return inventory.getStackInSlot(0);
    }

    public List<ItemStack> getReagents() {
        List<ItemStack> reagents = new ArrayList<>();
        for (int i = 1; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) reagents.add(stack);
        }
        return reagents;
    }

    public List<ItemStack> getOfferings() {
        List<ItemStack> offerings = new ArrayList<>();
        for (ResonantPedestalBlockEntity pedestal : pedestals) {
            ItemStack stack = pedestal.getItem();
            if (!stack.isEmpty()) offerings.add(stack);
        }
        return offerings;
    }

    public boolean hasItem(int slot) {
        return !inventory.getStackInSlot(slot).isEmpty();
    }

    // ==========================================
    // TICK & CRAFTING LOGIC
    // ==========================================

    public int getProcessTime() {
        return processTime;
    }

    public int getTotalProcessTime() {
        return totalProcessTime;
    }

    public float getInterpolatedProgress(float partialTick) {
        if (this.totalProcessTime <= 0 || this.processTime <= 0) return 0.0F;
        float interpolated = Mth.lerp(partialTick, (float) this.processTime - 1, (float) this.processTime);
        return Mth.clamp(interpolated / (float) this.totalProcessTime, 0.0F, 1.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResonantAltarBlockEntity altar) {
        if (level.isClientSide) return;

        // Re-evaluate structure linkage if marked dirty
        if (altar.structureDirty) {
            altar.updateSurroundingPedestals();
            altar.structureDirty = false;
        }

        // Crafting verification and execution
        if (altar.canCraft()) {
            altar.processTime++;

            // Ambient ritual effects
            if (level instanceof ServerLevel serverLevel) {
                altar.spawnRitualParticles(serverLevel, pos);
            }

            if (altar.processTime >= altar.totalProcessTime) {
                altar.finishCrafting();
                altar.resetCraftingState();
            }
            altar.setChanged();
        } else {
            if (altar.processTime > 0) {
                altar.resetCraftingState();
                altar.setChanged();
            }
        }
    }

    private boolean canCraft() {
        if (this.level == null || this.inventory.getStackInSlot(0).isEmpty()) {
            return false;
        }

        // Cache or search matching recipe
        if (this.activeRecipe == null) {
            this.activeRecipe = findMatchingRecipe().orElse(null);
            if (this.activeRecipe != null) {
                this.totalProcessTime = this.activeRecipe.getProcessTime();
            } else {
                return false;
            }
        }

        // Validate recipe match against current altar & pedestal items
        ResonantAltarRecipe.ResonantAltarRecipeInput input = createRecipeInput();
        if (!this.activeRecipe.matches(input, this.level)) {
            this.activeRecipe = null;
            return false;
        }

        return true;
    }

    private Optional<ResonantAltarRecipe> findMatchingRecipe() {
        if (this.level == null) return Optional.empty();

        ResonantAltarRecipe.ResonantAltarRecipeInput input = createRecipeInput();

        return this.level.getRecipeManager()
                .getRecipeFor(RecipesRegistries.RESONANT_ALTAR_TYPE.get(), input, this.level)
                .map(RecipeHolder::value);
    }

    private ResonantAltarRecipe.ResonantAltarRecipeInput createRecipeInput() {
        ItemStack baseInput = this.inventory.getStackInSlot(0);
        List<ItemStack> reagents = getReagents();
        List<ItemStack> offerings = getOfferings();

        return new ResonantAltarRecipe.ResonantAltarRecipeInput(baseInput, reagents, offerings);
    }

    private void finishCrafting() {
        if (this.activeRecipe == null || this.level == null) return;

        ResonantAltarRecipe.ResonantAltarRecipeInput input = createRecipeInput();
        ItemStack resultStack = this.activeRecipe.assemble(input, this.level.registryAccess());

        // 1. Clear 4 reagents inside the altar
        removeReagents();

        // 2. Consume 1 item from each bound pedestal
        removeOfferings();

        // 3. Set the crafted result into the center slot
        this.inventory.setStackInSlot(0, resultStack);

        // Completion burst and sound
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 1.2D,
                    this.worldPosition.getZ() + 0.5D,
                    24,
                    0.25D, 0.25D, 0.25D,
                    0.1D
            );
            serverLevel.playSound(
                    null,
                    this.worldPosition,
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
    }

    private void resetCraftingState() {
        this.processTime = 0;
        this.totalProcessTime = 0;
        this.activeRecipe = null;
    }

    private void spawnRitualParticles(ServerLevel level, BlockPos pos) {
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.85D;
        double centerZ = pos.getZ() + 0.5D;

        // Converging amethyst dust from pedestals to altar
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.72F, 0.25F, 0.95F), 0.8F);

        for (ResonantPedestalBlockEntity pedestal : this.pedestals) {
            if (pedestal.hasItem()) {
                BlockPos pPos = pedestal.getBlockPos();
                double pX = pPos.getX() + 0.5D;
                double pY = pPos.getY() + 1.2D;
                double pZ = pPos.getZ() + 0.5D;

                double velX = (centerX - pX) * 0.1D;
                double velY = (centerY - pY) * 0.1D;
                double velZ = (centerZ - pZ) * 0.1D;

                level.sendParticles(dust, pX, pY, pZ, 1, velX, velY, velZ, 0.05D);
            }
        }

        // Center spinning glyph particles
        if (level.getGameTime() % 4 == 0) {
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    centerX, centerY + 0.2D, centerZ,
                    4,
                    0.2D, 0.2D, 0.2D,
                    0.2D
            );
        }

        // Ambient sound
        if (level.getGameTime() % 40 == 0) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS,
                    0.7F,
                    1.0F + (float) this.processTime / Math.max(1, this.totalProcessTime)
            );
        }
    }

    // ==========================================
    // MULTIBLOCK & PEDESTAL BINDING
    // ==========================================

    public void markStructureDirty() {
        this.structureDirty = true;
        setChanged();
    }

    public List<ResonantPedestalBlockEntity> getPedestals() {
        return Collections.unmodifiableList(pedestals);
    }

    public void updateSurroundingPedestals() {
        pedestals.clear();
        if (this.level == null) return;

        for (BlockPos offset : ResonantHelper.PEDESTAL_OFFSETS) {
            BlockPos targetPos = this.worldPosition.offset(offset);

            if (this.level.isLoaded(targetPos)) {
                BlockEntity blockEntity = this.level.getBlockEntity(targetPos);
                if (blockEntity instanceof ResonantPedestalBlockEntity pedestal) {
                    if (!pedestal.hasAltar()) pedestals.add(pedestal);
                    else if (pedestal.isAltarAtPos(this.worldPosition)) pedestals.add(pedestal);
                }
            }
        }

        for (ResonantPedestalBlockEntity pedestal : pedestals) {
            pedestal.setAltar(this.worldPosition);
        }
    }

    public void unbindAndNotifyAllPedestals() {
        if (this.level == null || this.level.isClientSide) return;

        List<ResonantPedestalBlockEntity> boundPedestals = new ArrayList<>(this.pedestals);
        for (ResonantPedestalBlockEntity pedestal : boundPedestals) {
            if (pedestal.isAltarAtPos(this.worldPosition)) {
                pedestal.removeAltar();
                pedestal.notifyAltars();
            }
        }
        this.pedestals.clear();
        this.structureDirty = true;
    }

    // ==========================================
    // SYNC & NBT SERIALIZATION
    // ==========================================
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("ProcessTime", this.processTime);
        tag.putInt("TotalProcessTime", this.totalProcessTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        this.processTime = tag.getInt("ProcessTime");
        this.totalProcessTime = tag.getInt("TotalProcessTime");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(tag, lookupProvider);
        loadAdditional(tag, lookupProvider);
    }
}
