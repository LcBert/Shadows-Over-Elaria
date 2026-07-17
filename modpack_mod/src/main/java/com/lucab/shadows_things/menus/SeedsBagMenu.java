package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.content.item.Crops;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeedsBagMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 6;
    private static final int SLOT_COL = 3;

    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 68;

    private static final int GUI_WIDTH = 176;

    private final ItemStack bagStack;
    private final ItemStackHandler bagInventory;

    public SeedsBagMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public SeedsBagMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public SeedsBagMenu(int containerId, Inventory playerInventory, ItemStack bagStack) {
        super(MenuRegistries.SEEDS_BAG_MENU.get(), containerId);
        this.bagStack = bagStack;
        this.bagInventory = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < getSlots(); i++) {
                    stacks.add(getStackInSlot(i));
                }
                SeedsBagMenu.this.bagStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
            }
        };

        if (bagStack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = bagStack.get(DataComponents.CONTAINER);
            if (contents != null) {
                int slotIndex = 0;
                for (ItemStack item : contents.stream().toList()) {
                    if (slotIndex < this.bagInventory.getSlots()) {
                        this.bagInventory.setStackInSlot(slotIndex, item.copy());
                        slotIndex++;
                    }
                }
            }
        }

        // Bag Inventory
        int baseStartY = 18; // Altezza iniziale dall'alto del menu della sacca
        int totalRows = (int) Math.ceil((double) SLOT_COUNT / SLOT_COL);
        for (int row = 0; row < totalRows; row++) {
            int slotsInThisRow = Math.min(SLOT_COL, SLOT_COUNT - (row * SLOT_COL));
            int rowGridWidth = slotsInThisRow * 18;
            int rowStartX = (GUI_WIDTH / 2) - (rowGridWidth / 2) + 1;
            for (int col = 0; col < slotsInThisRow; col++) {
                int bagSlotIndex = col + (row * SLOT_COL);
                int xPos = rowStartX + (col * 18);
                int yPos = baseStartY + (row * 18);

                Item filterItem = getFilterItemForSlot(bagSlotIndex);

                this.addSlot(new SlotItemHandler(bagInventory, bagSlotIndex, xPos, yPos) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !stack.isEmpty() && stack.is(filterItem);
                    }
                });
            }
        }

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int playerSlotIndex = col + row * 9 + 9;
                int xPos = INVENTORY_X + (col * 18);
                int yPos = INVENTORY_Y + (row * 18);

                this.addSlot(new Slot(playerInventory, playerSlotIndex, xPos, yPos));
            }
        }

        // Player Hotbar
        int hotbarY = INVENTORY_Y + 58;
        for (int col = 0; col < 9; col++) {
            int xPos = INVENTORY_X + (col * 18);

            if (playerInventory.getItem(col) == bagStack) {
                this.addSlot(new Slot(playerInventory, col, xPos, hotbarY) {
                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, col, xPos, hotbarY));
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return !this.bagStack.isEmpty() && (player.getMainHandItem() == bagStack || player.getOffhandItem() == bagStack);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack quickmovedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            quickmovedStack = rawStack.copy();

            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(rawStack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(rawStack, 0, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (rawStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (rawStack.getCount() == quickmovedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, rawStack);
        }
        return quickmovedStack;
    }

    public ItemStackHandler getBagInventory() {
        return this.bagInventory;
    }

    public Item getFilterItemForSlot(int bagSlotIndex) {
        List<Item> allowedSeedsPerSlot = List.of(
                Items.WHEAT_SEEDS,
                Crops.CARROT_SEEDS.get(),
                Crops.POTATO_SEEDS.get(),
                Items.BEETROOT_SEEDS,
                Crops.ONION_SEEDS.get(),
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("farmersdelight:cabbage_seeds"))
        );
        if (bagSlotIndex >= 0 && bagSlotIndex < allowedSeedsPerSlot.size()) {
            return allowedSeedsPerSlot.get(bagSlotIndex);
        }
        return Items.AIR;
    }

}
