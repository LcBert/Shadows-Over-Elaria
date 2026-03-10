package com.lucab.shadows_over_elaria.item;

import java.util.Collection;
import java.util.List;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public class Crops {
    public static final DeferredItem<BlockItem> CARROT_SEEDS = ShadowsOverElaria.ITEMS.register(
            "carrot_seeds", () -> new CropItem("carrot_seeds", "minecraft:carrots"));

    public static final DeferredItem<BlockItem> POTATO_SEEDS = ShadowsOverElaria.ITEMS.register(
            "potato_seeds", () -> new CropItem("potato_seeds", "minecraft:potatoes"));

    public static void register() {
    }

    public static Collection<ItemStack> getItems() {
        return List.of(
                new ItemStack(CARROT_SEEDS.get()),
                new ItemStack(POTATO_SEEDS.get()));
    }
}
