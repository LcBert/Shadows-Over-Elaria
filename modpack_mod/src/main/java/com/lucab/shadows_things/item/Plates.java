package com.lucab.shadows_things.item;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class Plates {
    public static final DeferredItem<Item> COPPER_PLATE = ShadowsThings.ITEMS.register(
            "copper_plate", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_PLATE = ShadowsThings.ITEMS.register(
            "iron_plate", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_PLATE = ShadowsThings.ITEMS.register(
            "gold_plate", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_PLATE = ShadowsThings.ITEMS.register(
            "diamond_plate", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NETHERITE_PLATE = ShadowsThings.ITEMS.register(
            "netherite_plate", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(COPPER_PLATE.get()),
                new ItemStack(IRON_PLATE.get()),
                new ItemStack(GOLD_PLATE.get()),
                new ItemStack(DIAMOND_PLATE.get()),
                new ItemStack(NETHERITE_PLATE.get()));
    }
}
