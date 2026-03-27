package com.lucab.shadows_things.item;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class Crops {
    public static final DeferredItem<BlockItem> CARROT_SEEDS = ShadowsThings.ITEMS.register(
            "carrot_seeds", () -> new CropItem("carrot_seeds", "minecraft:carrots"));

    public static final DeferredItem<BlockItem> POTATO_SEEDS = ShadowsThings.ITEMS.register(
            "potato_seeds", () -> new CropItem("potato_seeds", "minecraft:potatoes"));

    public static final DeferredItem<BlockItem> ONION_SEEDS = ShadowsThings.ITEMS.register(
            "onion_seeds", () -> new CropItem("onion_seeds", "farmersdelight:onions"));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(CARROT_SEEDS.get()),
                new ItemStack(POTATO_SEEDS.get()),
                new ItemStack(ONION_SEEDS.get()));
    }
}
