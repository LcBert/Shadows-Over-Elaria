package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.content.block.oven.OvenBlockEntity;
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
import org.jetbrains.annotations.NotNull;

public class OvenMenu extends BaseMachineMenu {
    private final OvenBlockEntity blockEntity;

    public OvenMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public OvenMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(MenuRegistries.OVEN_MENU.get(), containerId, blockEntity,
                new int[]{0, 2}, new int[]{4, 6}, 3, 7);

        if (!(blockEntity instanceof OvenBlockEntity ovenEntity)) {
            throw new IllegalStateException("BlockEntity is not an instance of OvenBlockEntity!");
        }

        this.blockEntity = ovenEntity;

        IItemHandler itemHandler = ovenEntity.getInventoryHandler();

        int inputX = 43;
        int inputY = 18;
        int inputSpacing = 4;

        int fuelX = 117;
        int fuelY = 38;

        int outputX = 43;
        int outputY = 60;
        int outputSpacing = 4;

        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i, inputX + (i * 18) + (inputSpacing * i), inputY));
        }

        this.addSlot(new SlotItemHandler(itemHandler, 3, fuelX, fuelY));

        for (int i = 0; i < 3; i++) {
            int outputIndex = 4 + i;
            this.addSlot(new SlotItemHandler(itemHandler, outputIndex, outputX + (i * 18) + (outputSpacing * i), outputY) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
        }

        this.addDataSlots(ovenEntity.getContainerData());
    }

    public OvenBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
