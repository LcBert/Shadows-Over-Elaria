package com.lucab.shadows_over_elaria.block.repair_table;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;
import com.lucab.shadows_over_elaria.block.BlocksRegister;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
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
                if (stack.is(ItemTags.create(
                        ResourceLocation.fromNamespaceAndPath(ShadowsOverElaria.MODID, "repair_kits")))) {
                    return true;
                }
                return false;
            }
            return true;
        };
    };

    public ItemStack insertKit(ItemStack stack) {
        return this.inventory.insertItem(0, stack.copy(), false);
    }

    public ItemStack removeKit() {
        ItemStack returnStack = this.inventory.extractItem(0, 64, false);
        return returnStack;
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
        ItemStack returnStack = this.inventory.extractItem(1, 1, false);
        return returnStack;
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

    public boolean repair(Level level, BlockPos pos, BlockState state) {
        if (!this.hasKit() || !this.hasItem())
            return false;

        ItemStack item = this.getItem();

        int damage = item.getDamageValue();
        int maxDamage = item.getMaxDamage();
        float damageRatio = ((float) damage / (float) maxDamage);

        System.out.println(damage);
        System.out.println(maxDamage);
        System.out.println(damageRatio);

        if (damageRatio >= 0.25) {
            item.setDamageValue((int) (damage - (maxDamage * 0.25)));
            this.consumeKit();
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS);
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
