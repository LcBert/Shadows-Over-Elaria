package com.lucab.shadows_things.menus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ProfessionMenu extends AbstractContainerMenu {
    private final ContainerData data;

    public ProfessionMenu(int containerId, Inventory playerInventory, ContainerData data) {
        super(MenuRegistries.PROFESSION_MENU.get(), containerId);
        this.data = data;
        this.addDataSlots(this.data);
    }

    public ProfessionMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainerData(6));
    }

    public ContainerData getData() {
        return this.data;
    }

    public int getExperience() {
        return this.data.get(0);
    }

    public int getExperienceRequired() {
        return this.data.get(1);
    }

    public int getPoints() {
        return this.data.get(2);
    }

    public int getBlacksmithLevel() {
        return this.data.get(3);
    }

    public int getFarmerLevel() {
        return this.data.get(4);
    }

    public int getCookLevel() {
        return this.data.get(5);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public void broadcastChanges() {
        super.broadcastChanges();
    }
}
