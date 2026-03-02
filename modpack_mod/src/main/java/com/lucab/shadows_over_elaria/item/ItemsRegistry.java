package com.lucab.shadows_over_elaria.item;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemsRegistry {
    public static final DeferredItem<BlockItem> CARROT_SEED = ShadowsOverElaria.ITEMS.register(
            "carrot_seed",
            () -> new BlockItem(Blocks.CARROTS, new Item.Properties()) {
                @Override
                public String getDescriptionId() {
                    return "item.shadows_over_elaria.carrot_seed";
                }
            });

    public static final DeferredItem<BlockItem> POTATO_SEED = ShadowsOverElaria.ITEMS.register(
            "potato_seed",
            () -> new BlockItem(Blocks.POTATOES, new Item.Properties()) {
                @Override
                public String getDescriptionId() {
                    return "item.shadows_over_elaria.potato_seed";
                }
            });

    public static void init() {
    }
}
