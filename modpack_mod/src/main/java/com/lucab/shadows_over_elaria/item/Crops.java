package com.lucab.shadows_over_elaria.item;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

public class Crops {
    public static final DeferredItem<BlockItem> CARROT_SEEDS = ShadowsOverElaria.ITEMS.register(
            "carrot_seeds",
            () -> new BlockItem(Blocks.CARROTS, new Item.Properties()) {
                @Override
                public String getDescriptionId() {
                    return "item.shadows_over_elaria.carrot_seeds";
                }
            });

    public static final DeferredItem<BlockItem> POTATO_SEEDS = ShadowsOverElaria.ITEMS.register(
            "potato_seeds",
            () -> new BlockItem(Blocks.POTATOES, new Item.Properties()) {
                @Override
                public String getDescriptionId() {
                    return "item.shadows_over_elaria.potato_seeds";
                }
            });

    public static void init() {
    }
}
