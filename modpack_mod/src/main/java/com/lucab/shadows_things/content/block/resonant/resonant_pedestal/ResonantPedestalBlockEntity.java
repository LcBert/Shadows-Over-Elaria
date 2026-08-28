package com.lucab.shadows_things.content.block.resonant.resonant_pedestal;

import com.lucab.shadows_things.content.block.resonant.ResonantHelper;
import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ResonantPedestalBlockEntity extends BlockEntity {
    private BlockPos altarPos;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ResonantPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ResonantPedestalRegistry.RESONANT_PEDESTAL_ENTITY.get(), pos, state);
    }

    public void notifyAltars() {
        if (this.level == null || this.level.isClientSide) return;

        for (BlockPos offset : ResonantHelper.PEDESTAL_OFFSETS) {
            BlockPos targetPos = this.worldPosition.offset(offset);

            if (this.level.isLoaded(targetPos)) {
                BlockEntity blockEntity = this.level.getBlockEntity(targetPos);
                if (blockEntity instanceof ResonantAltarBlockEntity altar) {
                    altar.markStructureDirty();
                }
            }
        }
    }

    public boolean insertItem(ItemStack stack) {
        if (!hasItem()) {
            inventory.setStackInSlot(0, stack.copyWithCount(1));
            return true;
        }
        return false;
    }

    public ItemStack removeItem() {
        return inventory.extractItem(0, 1, false);
    }

    public ItemStack getItem() {
        return inventory.getStackInSlot(0);
    }

    public boolean hasItem() {
        return !inventory.getStackInSlot(0).isEmpty();
    }

    public void setAltar(BlockPos pos) {
        altarPos = pos;
        setChanged();
    }

    public void removeAltar() {
        altarPos = null;
        setChanged();
    }

    public BlockPos getAltar() {
        return altarPos;
    }

    public boolean hasAltar() {
        return altarPos != null;
    }

    public boolean isAltarAtPos(BlockPos pos) {
        return hasAltar() && altarPos.equals(pos);
    }

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
        if (altarPos != null) tag.put("AltarPos", NbtUtils.writeBlockPos(altarPos));

    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory"))
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));

        if (tag.contains("AltarPos"))
            NbtUtils.readBlockPos(tag, "AltarPos").ifPresent(pos -> altarPos = pos);
        else
            altarPos = null;
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
