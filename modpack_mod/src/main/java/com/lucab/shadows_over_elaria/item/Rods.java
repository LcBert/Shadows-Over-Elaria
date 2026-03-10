package com.lucab.shadows_over_elaria.item;

import java.util.Collection;
import java.util.List;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class Rods {
    public static final DeferredItem<Item> COPPER_ROD = ShadowsOverElaria.ITEMS.register(
            "copper_rod", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_ROD = ShadowsOverElaria.ITEMS.register(
            "iron_rod", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_ROD = ShadowsOverElaria.ITEMS.register(
            "gold_rod", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_ROD = ShadowsOverElaria.ITEMS.register(
            "diamond_rod", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NETHERITE_ROD = ShadowsOverElaria.ITEMS.register(
            "netherite_rod", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static Collection<ItemStack> getItems() {
        return List.of(
                new ItemStack(COPPER_ROD.get()),
                new ItemStack(IRON_ROD.get()),
                new ItemStack(GOLD_ROD.get()),
                new ItemStack(DIAMOND_ROD.get()),
                new ItemStack(NETHERITE_ROD.get()));
    }
}
