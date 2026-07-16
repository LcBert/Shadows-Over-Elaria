package com.lucab.shadows_things.content;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class SilverSet {
    // Silver Ore
    public static final DeferredBlock<Block> SILVER_ORE = ShadowsThings.BLOCKS.register("silver_ore",
            () -> new Block(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .strength(2.0f)));

    public static final DeferredItem<BlockItem> SILVER_ORE_ITEM = ShadowsThings.ITEMS.register("silver_ore",
            () -> new BlockItem(SILVER_ORE.get(), new Item.Properties()));

    public static final DeferredItem<Item> RAW_SILVER = ShadowsThings.ITEMS.register("raw_silver",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SILVER_INGOT = ShadowsThings.ITEMS.register("silver_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SILVER_NUGGET = ShadowsThings.ITEMS.register("silver_nugget",
            () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SILVER_ORE_ITEM.get()),
                new ItemStack(RAW_SILVER.get()),
                new ItemStack(SILVER_INGOT.get()),
                new ItemStack(SILVER_NUGGET.get())
        );
    }
}
