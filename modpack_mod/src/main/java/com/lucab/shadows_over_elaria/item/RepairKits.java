package com.lucab.shadows_over_elaria.item;

import java.util.Collection;
import java.util.List;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class RepairKits {
    public static final DeferredItem<Item> COPPER_REPAIR_KIT = ShadowsOverElaria.ITEMS.register(
            "copper_repair_kit", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_REPAIR_KIT = ShadowsOverElaria.ITEMS.register(
            "iron_repair_kit", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_REPAIR_KIT = ShadowsOverElaria.ITEMS.register(
            "gold_repair_kit", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_REPAIR_KIT = ShadowsOverElaria.ITEMS.register(
            "diamond_repair_kit", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NETHERITE_REPAIR_KIT = ShadowsOverElaria.ITEMS.register(
            "netherite_repair_kit", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static Collection<ItemStack> getItems() {
        return List.of(
                new ItemStack(COPPER_REPAIR_KIT.get()),
                new ItemStack(IRON_REPAIR_KIT.get()),
                new ItemStack(GOLD_REPAIR_KIT.get()),
                new ItemStack(DIAMOND_REPAIR_KIT.get()),
                new ItemStack(NETHERITE_REPAIR_KIT.get()));
    }
}
