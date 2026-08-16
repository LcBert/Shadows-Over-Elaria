package com.lucab.shadows_things.content.block.cauldron;

import com.lucab.shadows_things.content.item.GlassBottles;
import com.lucab.shadows_things.recipe.CauldronRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CauldronBlockEntity extends BlockEntity {
    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(CauldronRegister.CAULDRON_BLOCK_ENTITY.get(), pos, state);
    }

    private int litTime;
    private int litDuration = 200;
    private int processTime;
    private int totalProcessTime = 200;

    private CauldronRecipe.RecipeInstance recipeInstance = null;

    public final ItemStackHandler inventory = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // Input slots
            if (slot >= 0 && slot <= 5) return true;
            // Fuel Slot
            if (slot == 6) return stack.getBurnTime(null) > 0;
            return super.isItemValid(slot, stack);
        }

    };

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CauldronBlockEntity.this.litTime;
                case 1 -> CauldronBlockEntity.this.litDuration;
                case 2 -> CauldronBlockEntity.this.processTime;
                case 3 -> CauldronBlockEntity.this.totalProcessTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CauldronBlockEntity.this.litTime = value;
                case 1 -> CauldronBlockEntity.this.litDuration = value;
                case 2 -> CauldronBlockEntity.this.processTime = value;
                case 3 -> CauldronBlockEntity.this.totalProcessTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public int getLitTime() {
        return this.containerData.get(0);
    }

    public int getLitDuration() {
        return this.containerData.get(1);
    }

    public int getProcessTime() {
        return this.containerData.get(2);
    }

    public int getTotalProcessTime() {
        return this.containerData.get(3);
    }


    public IItemHandler getInventoryHandler() {
        return this.inventory;
    }

    public ContainerData getContainerData() {
        return this.containerData;
    }

    private int waterLevel = 0;
    private PotionContents potionContents = PotionContents.EMPTY;

    public boolean hasWater() {
        return waterLevel == 3;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public boolean insertWaterBucket() {
        if (waterLevel == 3 || !potionContents.equals(PotionContents.EMPTY)) return false;
        waterLevel = 3;
        this.potionContents = new PotionContents(Potions.WATER);
        setChanged();
        return true;
    }

    public boolean extractWaterBucket() {
        if (waterLevel < 3 || !potionContents.is(Potions.WATER)) return false;
        waterLevel = 0;
        this.potionContents = PotionContents.EMPTY;
        setChanged();
        return true;
    }

    public ItemStack getBottleWithPotion(ItemStack stack) {
        if (waterLevel == 0) return ItemStack.EMPTY;
        if (processTime > 0) return ItemStack.EMPTY;
        waterLevel--;

        Item item;
        if (stack.is(Items.GLASS_BOTTLE)) item = Items.POTION;
        else if (stack.is(GlassBottles.SPLASH_GLASS_BOTTLE)) item = Items.SPLASH_POTION;
        else if (stack.is(GlassBottles.LINGERING_GLASS_BOTTLE)) item = Items.LINGERING_POTION;
        else return ItemStack.EMPTY;

        ItemStack potionStack = new ItemStack(item);
        potionStack.set(DataComponents.POTION_CONTENTS, this.potionContents);
        if (waterLevel == 0) this.potionContents = PotionContents.EMPTY;
        setChanged();
        return potionStack.copy();
    }

    public PotionContents getPotionContents() {
        return this.potionContents;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CauldronBlockEntity cauldron) {
        if (level.isClientSide) return;

        cauldron.handleLitState();
        if (cauldron.canProcess()) {
            cauldron.setChanged();
            if (cauldron.isLit()) cauldron.processTime++;
            else if (level.getGameTime() % 20 == 0 && cauldron.processTime > 0) cauldron.processTime--;
            if (cauldron.processTime >= cauldron.totalProcessTime) {
                cauldron.processRecipe();
                cauldron.processTime = 0;
            }
        } else {
            cauldron.processTime = 0;
        }

        cauldron.processFuel();
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    private void handleLitState() {
        BlockState currentState = getBlockState();
        if (currentState.getValue(CauldronBlock.LIT) != isLit()) {
            level.setBlock(getBlockPos(), currentState.setValue(CauldronBlock.LIT, isLit()), Block.UPDATE_ALL);
            setChanged();
        }
    }

    private void processFuel() {
        if (!(level.getBlockState(getBlockPos()).getBlock() instanceof CauldronBlock cauldronBlock)) return;

        if (!isLit() && canProcess()) {
            ItemStack fuelStack = inventory.getStackInSlot(6);
            if (!fuelStack.isEmpty()) {
                int burnTime = fuelStack.getBurnTime(null);
                if (burnTime > 0) {
                    fuelStack.shrink(1);
                    litDuration = burnTime;
                    litTime = burnTime;
                }
            }
        } else {
            if (litTime > 0) litTime--;
        }
    }

    private void findRecipe() {
        Map<Integer, ItemStack> inputStacks = new HashMap<>();
        int inputStartSlot = 0;
        int inputEndSlot = 5;
        for (int slot = inputStartSlot; slot <= inputEndSlot; slot++) {
            ItemStack inputStack = this.inventory.getStackInSlot(slot);
            if (!inputStack.isEmpty()) inputStacks.put(slot, this.inventory.getStackInSlot(slot));
        }

        CauldronRecipe recipe = CauldronRecipe.getRecipe(level, inputStacks.values().stream().toList());
        if (recipe == null) return;

        Map<Integer, Integer> usedSlots = new HashMap<>();
        Map<Integer, ItemStack> stackSlots = new HashMap<>();
        for (SizedIngredient sized : recipe.getIngredientsList()) {
            int requiredCount = sized.count();
            int foundCount = 0;

            for (int i = inputStartSlot; i <= inputEndSlot; i++) {
                ItemStack stackInSlot = this.inventory.getStackInSlot(i);

                if (sized.ingredient().test(stackInSlot)) {
                    int canTake = Math.min(stackInSlot.getCount(), requiredCount - foundCount);
                    foundCount += canTake;
                    usedSlots.put(i, canTake);
                    stackSlots.put(i, stackInSlot);
                }

                if (foundCount >= requiredCount) break;
            }
        }

        recipeInstance = new CauldronRecipe.RecipeInstance(
                usedSlots,
                stackSlots,
                recipe.getResultItem(null),
                recipe.getProcessTime()
        );
    }

    private boolean canProcess() {
        if (!hasWater()) {
            recipeInstance = null;
            return false;
        }

        if (recipeInstance == null) {
            findRecipe();
            return false;
        }

        if (!potionContents.is(Potions.WATER)) return false;

        this.totalProcessTime = recipeInstance.processTime;

        Map<Integer, ItemStack> stackSlots = recipeInstance.stackSlots;

        // Check if input items is removed
        for (Map.Entry<Integer, ItemStack> entry : stackSlots.entrySet()) {
            int recipeSlot = entry.getKey();
            ItemStack recipeStack = entry.getValue();
            if (!ItemStack.isSameItemSameComponents(recipeStack, this.inventory.getStackInSlot(recipeSlot))) {
                recipeInstance = null;
                return false;
            }
            if (recipeStack.getCount() > this.inventory.getStackInSlot(recipeSlot).getCount()) {
                recipeInstance = null;
                return false;
            }
        }

        return true;
    }

    private void processRecipe() {
        if (!canProcess() || recipeInstance == null) return;
        Map<Integer, Integer> usedSlots = recipeInstance.inputSlotCount;
        ItemStack recipeOutput = recipeInstance.outputStack;


        for (Map.Entry<Integer, Integer> entry : usedSlots.entrySet()) {
            int slot = entry.getKey();
            int count = entry.getValue();
            this.inventory.getStackInSlot(slot).shrink(count);
        }

        potionContents = recipeOutput.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        recipeInstance = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("WaterLevel", waterLevel);
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("ProcessTime", processTime);
        tag.putInt("TotalProcessTime", totalProcessTime);
        tag.put("PotionContents", PotionContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), potionContents).getOrThrow());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        waterLevel = tag.getInt("WaterLevel");
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        processTime = tag.getInt("ProcessTime");
        totalProcessTime = tag.getInt("TotalProcessTime");
        if (tag.contains("PotionContents")) {
            this.potionContents = PotionContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("PotionContents")).getOrThrow();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
