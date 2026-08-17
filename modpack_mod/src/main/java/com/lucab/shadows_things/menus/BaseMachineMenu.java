package com.lucab.shadows_things.menus;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BaseMachineMenu extends AbstractContainerMenu {
    private final BlockEntity blockEntity;
    private final ContainerLevelAccess access;

    protected final int[] inputSlotIndexes;
    protected final int[] outputSlotIndexes;
    protected final int fuelSlotIndex;
    protected final int slotsCount;

    protected BaseMachineMenu(@Nullable MenuType<?> menuType, int containerId, BlockEntity blockEntity,
                              int[] inputSlotIndexes, int[] outputSlotIndexes, int fuelSlotIndex, int slotsCount) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.inputSlotIndexes = inputSlotIndexes;
        this.outputSlotIndexes = outputSlotIndexes;
        this.fuelSlotIndex = fuelSlotIndex;
        this.slotsCount = slotsCount;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            quickMovedStack = rawStack.copy();

            if (index < slotsCount) {
                // From block to player
                if (!this.moveItemStackTo(rawStack, slotsCount, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(rawStack, quickMovedStack);
            } else {
                // From player to block
                if (!this.moveItemStackTo(rawStack, fuelSlotIndex, fuelSlotIndex + 1, false)) {
                    if (!this.moveItemStackTo(rawStack, inputSlotIndexes[0], inputSlotIndexes[1] + 1, false)) {
                        if (!this.moveItemStackTo(rawStack, outputSlotIndexes[0], outputSlotIndexes[1] + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (rawStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (rawStack.getCount() == quickMovedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, rawStack);
        }
        return quickMovedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProcessTime() {
        return -1;
    }

    public int getTotalProcessTime() {
        return -1;
    }

    public int getLitTime() {
        return -1;
    }

    public int getLitDuration() {
        return -1;
    }
}
