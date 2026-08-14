package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.content.block.smeltery.SmelteryBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SmelteryMenu extends AbstractContainerMenu {
    private final SmelteryBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public SmelteryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public SmelteryMenu(int containerId, Inventory playerInventory, BlockEntity entity) {
        super(MenuRegistries.SMELTERY_MENU.get(), containerId);

        if (!(entity instanceof SmelteryBlockEntity smelteryEntity)) {
            throw new IllegalStateException("BlockEntity is not an instance of SmelteryBlockEntity");
        }

        this.blockEntity = smelteryEntity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        IItemHandler itemHandler = smelteryEntity.getInventoryHandler();

        int inputStartX = 22;
        int inputStartY = 28;
        int inputRow = 3;
        int inputColumn = 6;

        int outputX = 152;
        int outputY = 28;

        int fuelX = 152;
        int fuelY = 64;

        // Input Slots
        int slotIndex = 0;
        for (int row = 0; row < inputRow; row++) {
            for (int col = 0; col < inputColumn; col++) {
                this.addSlot(new SlotItemHandler(itemHandler, slotIndex++, inputStartX + (col * 18), inputStartY + (row * 18)));
            }
        }

        // Output Slot
        this.addSlot(new SlotItemHandler(itemHandler, slotIndex++, outputX, outputY));
        this.addSlot(new SlotItemHandler(itemHandler, slotIndex++, fuelX, fuelY));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
        }

        this.addDataSlots(smelteryEntity.getContainerData());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            quickMovedStack = rawStack.copy();

            if (index < 20) {
                // From block to player
                if (!this.moveItemStackTo(rawStack, 18, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(rawStack, quickMovedStack);
            } else {
                // From player to block
                Level level = this.blockEntity.getLevel();
                boolean isFuel = rawStack.getBurnTime(null) > 0;
                if (level != null && isFuel) {
                    // First try to insert fuel items into fuel slot
                    if (!this.moveItemStackTo(rawStack, 19, 20, false)) {
                        if (!this.moveItemStackTo(rawStack, 0, 18, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // If is not a fuel insert into Input Slots
                    if (!this.moveItemStackTo(rawStack, 0, 18, false)) {
                        return ItemStack.EMPTY;
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

    public SmelteryBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
