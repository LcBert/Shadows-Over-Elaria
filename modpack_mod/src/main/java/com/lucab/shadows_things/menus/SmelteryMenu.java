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

public class SmelteryMenu extends BaseMachineMenu {
    private final SmelteryBlockEntity blockEntity;

    public SmelteryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public SmelteryMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(MenuRegistries.SMELTERY_MENU.get(), containerId, blockEntity,
                new int[]{0, 17}, new int[]{18, 18}, 19, 20);

        if (!(blockEntity instanceof SmelteryBlockEntity smelteryEntity)) {
            throw new IllegalStateException("BlockEntity is not an instance of SmelteryBlockEntity");
        }

        this.blockEntity = smelteryEntity;

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

        // Fuel Slot
        this.addSlot(new SlotItemHandler(itemHandler, slotIndex++, fuelX, fuelY));

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
        }

        this.addDataSlots(smelteryEntity.getContainerData());
    }

    @Override
    public SmelteryBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getProcessTime() {
        return blockEntity.getProcessTime();
    }

    @Override
    public int getTotalProcessTime() {
        return blockEntity.getTotalProcessTime();
    }

    @Override
    public int getLitTime() {
        return blockEntity.getLitTime();
    }

    @Override
    public int getLitDuration() {
        return blockEntity.getLitDuration();
    }
}
