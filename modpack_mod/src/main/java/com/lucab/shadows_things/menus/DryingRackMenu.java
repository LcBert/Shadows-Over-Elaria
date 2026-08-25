package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.content.block.drying_rack.DryingRackBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DryingRackMenu extends AbstractContainerMenu {
    private final DryingRackBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public DryingRackMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public DryingRackMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(MenuRegistries.DRYING_RACK_MENU.get(), containerId);

        if (!(blockEntity instanceof DryingRackBlockEntity rack))
            throw new IllegalArgumentException("BlockEntity must be instance of DryingRackBlockEntity");

        this.blockEntity = rack;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        IItemHandler itemHandler = rack.getInventoryHandler();

        int inputX = 44;
        int inputY = 21;
        int inputCount = 5;

        int outputX = 44;
        int outputY = 64;
        int outputCount = 5;

        // Input Slots
        for (int i = 0; i < inputCount; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i, inputX + i * 18, inputY));
        }

        // Output Slots
        for (int i = 0; i < outputCount; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i + inputCount, outputX + i * 18, outputY));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
        }
    }

    public DryingRackBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }
}
