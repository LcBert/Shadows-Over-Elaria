package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.content.block.cauldron.CauldronBlockEntity;
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

public class CauldronMenu extends BaseMachineMenu {
    private final CauldronBlockEntity blockEntity;

    public CauldronMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public CauldronMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(MenuRegistries.CAULDRON_MENU.get(), containerId, blockEntity,
                new int[]{0, 5}, new int[]{0, 0}, 6, 7);

        if (!(blockEntity instanceof CauldronBlockEntity cauldronEntity)) {
            throw new IllegalStateException("BlockEntity is not an instance of CauldronBlockEntity");
        }

        this.blockEntity = cauldronEntity;

        IItemHandler itemHandler = cauldronEntity.getInventoryHandler();

        int inputStartX = 62;
        int inputStartY = 17;
        int inputRow = 2;
        int inputColumn = 3;

        int fuelX = 80;
        int fuelY = 70;

        // Input Slots
        int slotIndex = 0;
        for (int row = 0; row < inputRow; row++) {
            for (int col = 0; col < inputColumn; col++) {
                this.addSlot(new SlotItemHandler(itemHandler, slotIndex++, inputStartX + (col * 18), inputStartY + (row * 18)));
            }
        }

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

        this.addDataSlots(cauldronEntity.getContainerData());
    }

    @Override
    public CauldronBlockEntity getBlockEntity() {
        return this.blockEntity;
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
