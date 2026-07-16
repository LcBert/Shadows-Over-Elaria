package com.lucab.shadows_things.block.repair_table;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.block.BlocksRegister;

import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class RepairTableEntity extends BlockEntity {
    public RepairTableEntity(BlockPos pos, BlockState state) {
        super(BlocksRegister.REPAIR_TABLE_ENTITY.get(), pos, state);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            if (level == null)
                return true;

            if (slot == 0) {
                return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "repair_kits")));
            }
            return true;
        }
    };

    public ItemStack insertKit(ItemStack stack) {
        return this.inventory.insertItem(0, stack.copy(), false);
    }

    public ItemStack removeKit() {
        return this.inventory.extractItem(0, 64, false);
    }

    public void consumeKit() {
        this.inventory.extractItem(0, 1, false);
    }

    public ItemStack getKit() {
        return this.inventory.getStackInSlot(0);
    }

    public boolean hasKit() {
        return !this.inventory.getStackInSlot(0).isEmpty();
    }

    public ItemStack insertItem(ItemStack stack) {
        return this.inventory.insertItem(1, stack.copy(), false);
    }

    public ItemStack removeItem() {
        return this.inventory.extractItem(1, 1, false);
    }

    public ItemStack getItem() {
        return this.inventory.getStackInSlot(1);
    }

    public boolean hasItem() {
        return !this.inventory.getStackInSlot(1).isEmpty();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean repair(Level level, BlockPos pos, BlockState state, Player player) {
        if (!this.hasKit() || !this.hasItem()) return false;

        ItemStack item = this.getItem();

        int damage = item.getDamageValue();
        int maxDamage = item.getMaxDamage();
        float damageRatio = ((float) damage / (float) maxDamage);

        if (damageRatio >= 0.25) {
            int blacksmithLevel = ProfessionHelper.getLevel(player, ProfessionHelper.Professions.BLACKSMITH);

            // Handle Repair
            float repairEfficiency = 0.25F + ProfessionHelper.getPol(ProfessionHelper.BLACKSMITH_CHANCE.repair_efficiency, blacksmithLevel);

            double saveKitChance = ProfessionHelper.getPol(ProfessionHelper.BLACKSMITH_CHANCE.save_kit, blacksmithLevel);

            item.setDamageValue((int) (damage - (maxDamage * repairEfficiency)));
            if (level.random.nextDouble() >= saveKitChance) this.consumeKit();
            else setChanged();
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        12,
                        0.2, 0.05, 0.2,
                        0.05
                );
            }

            // Handle Experience
            float gainedXp = ProfessionHelper.getPol(ProfessionHelper.EXPERIENCE_PER_LEVEL, blacksmithLevel);
            ProfessionHelper.addExperience(player, (int) gainedXp);
            ProfessionHelper.tryLevelUp(player, true, true);

            return true;
        }

        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory"))
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag, lookupProvider);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(tag, lookupProvider);
        loadAdditional(tag, lookupProvider);
    }
}
