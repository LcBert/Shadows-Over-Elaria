package com.lucab.shadows_things.content.block.drying_rack;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class DryingRackRegister {
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK_BLOCK = ShadowsThings.BLOCKS.register(
            "drying_rack", () -> new DryingRackBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BLOCK_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "drying_rack", () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new,
                    DRYING_RACK_BLOCK.get()).build(null));

    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM = ShadowsThings.ITEMS.register(
            "drying_rack", () -> new BlockItem(DRYING_RACK_BLOCK.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(DRYING_RACK_ITEM.get())
        );
    }
}
