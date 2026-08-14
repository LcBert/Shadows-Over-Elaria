package com.lucab.shadows_things.content.block.smeltery;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class SmelteryRegister {
    public static final DeferredBlock<SmelteryBlock> STONE_SMELTERY = ShadowsThings.BLOCKS.register(
            "stone_smeltery", () -> new SmelteryBlock(1, Blocks.STONE_BRICKS));

    public static final DeferredBlock<SmelteryBlock> BRICK_SMELTERY = ShadowsThings.BLOCKS.register(
            "brick_smeltery", () -> new SmelteryBlock(2, Blocks.BRICKS));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmelteryBlockEntity>> SMELTERY_BLOCK_ENTITY = ShadowsThings.BLOCK_ENTITIES
            .register("smeltery_be",
                    () -> BlockEntityType.Builder.of(SmelteryBlockEntity::new,
                            STONE_SMELTERY.get(),
                            BRICK_SMELTERY.get()
                    ).build(null));

    public static final DeferredItem<BlockItem> STONE_SMELTERY_ITEM = ShadowsThings.ITEMS.register("stone_smeltery",
            () -> new BlockItem(STONE_SMELTERY.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> BRICK_SMELTERY_ITEM = ShadowsThings.ITEMS.register("brick_smeltery",
            () -> new BlockItem(BRICK_SMELTERY.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<SmelteryBlock> getSmelteries() {
        return List.of(
                STONE_SMELTERY.get(),
                BRICK_SMELTERY.get()
        );
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(STONE_SMELTERY_ITEM.get()),
                new ItemStack(BRICK_SMELTERY_ITEM.get())
        );
    }
}
