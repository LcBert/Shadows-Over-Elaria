package com.lucab.shadows_things.content.block.oven;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class OvenRegister {
    public static final DeferredBlock<OvenBlock> OVEN_BlOCK = ShadowsThings.BLOCKS.register(
            "oven", () -> new OvenBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OvenBlockEntity>> OVEN_BLOCK_ENTITY = ShadowsThings.BLOCK_ENTITIES
            .register("oven",
                    () -> BlockEntityType.Builder.of(OvenBlockEntity::new,
                            OVEN_BlOCK.get()).build(null));

    public static final DeferredItem<BlockItem> OVEN_BLOCK_ITEM = ShadowsThings.ITEMS.register("oven",
            () -> new BlockItem(OVEN_BlOCK.get(), new Item.Properties())
    );

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(OVEN_BLOCK_ITEM.get())
        );
    }
}
