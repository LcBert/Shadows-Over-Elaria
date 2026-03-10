package com.lucab.shadows_things.item;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class Hilts {
    public static final DeferredItem<Item> COPPER_HILT = ShadowsThings.ITEMS.register(
            "copper_hilt", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_HILT = ShadowsThings.ITEMS.register(
            "iron_hilt", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_HILT = ShadowsThings.ITEMS.register(
            "gold_hilt", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_HILT = ShadowsThings.ITEMS.register(
            "diamond_hilt", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NETHERITE_HILT = ShadowsThings.ITEMS.register(
            "netherite_hilt", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(COPPER_HILT.get()),
                new ItemStack(IRON_HILT.get()),
                new ItemStack(GOLD_HILT.get()),
                new ItemStack(DIAMOND_HILT.get()),
                new ItemStack(NETHERITE_HILT.get()));
    }
}
