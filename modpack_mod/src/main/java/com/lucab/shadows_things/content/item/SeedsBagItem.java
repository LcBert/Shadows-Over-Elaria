package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.SeedsBagMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeedsBagItem extends Item {
    public static final DeferredItem<Item> SEEDS_BAG = ShadowsThings.ITEMS.register("seeds_bag", SeedsBagItem::new);

    public SeedsBagItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack bagStack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            if (!player.isShiftKeyDown()) {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new SeedsBagMenu(containerId, playerInventory, bagStack),
                        Component.translatable("item.shadows_things.seeds_bag")
                ));
                player.playNotifySound(SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                if (insertSeedsInBag(player, bagStack))
                    player.playNotifySound(SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return InteractionResultHolder.sidedSuccess(bagStack, level.isClientSide);
    }

    private static boolean insertSeedsInBag(Player player, ItemStack bagStack) {
        Inventory playerInv = player.getInventory();

        ItemStackHandler tempHandler = new ItemStackHandler(SeedsBagMenu.SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (stack.isEmpty()) return false;
                Item filterItem = SeedsSlot.getFilterItemForSlot(slot);
                return stack.is(filterItem);
            }
        };

        if (bagStack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = bagStack.get(DataComponents.CONTAINER);
            if (contents != null) {
                List<ItemStack> items = contents.stream().toList();
                for (int i = 0; i < Math.min(items.size(), tempHandler.getSlots()); i++) {
                    tempHandler.setStackInSlot(i, items.get(i).copy());
                }
            }
        }

        boolean anyAdded = false;

        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            ItemStack invStack = playerInv.getItem(i);

            if (invStack.isEmpty() || invStack == bagStack) {
                continue;
            }

            for (int bagSlot = 0; bagSlot < SeedsBagMenu.SLOT_COUNT; bagSlot++) {
                Item filterItem = SeedsSlot.getFilterItemForSlot(bagSlot);

                if (invStack.is(filterItem)) {
                    ItemStack remainder = tempHandler.insertItem(bagSlot, invStack, false);

                    if (remainder.getCount() != invStack.getCount()) {
                        playerInv.setItem(i, remainder);
                        anyAdded = true;
                    }
                }
            }
        }

        if (anyAdded) {
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < tempHandler.getSlots(); i++) {
                list.add(tempHandler.getStackInSlot(i));
            }
            bagStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
        }

        return anyAdded;
    }

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SEEDS_BAG.get())
        );
    }
}
