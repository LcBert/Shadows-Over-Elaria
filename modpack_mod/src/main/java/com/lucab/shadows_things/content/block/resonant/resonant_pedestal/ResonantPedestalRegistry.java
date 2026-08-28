package com.lucab.shadows_things.content.block.resonant.resonant_pedestal;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ResonantPedestalRegistry {
    public static final DeferredBlock<ResonantPedestalBlock> RESONANT_PEDESTAL = ShadowsThings.BLOCKS.register(
            "resonant_pedestal", () -> new ResonantPedestalBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantPedestalBlockEntity>> RESONANT_PEDESTAL_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "resonant_pedestal", () -> BlockEntityType.Builder.of(ResonantPedestalBlockEntity::new,
                    RESONANT_PEDESTAL.get()).build(null));

    public static DeferredItem<BlockItem> RESONANT_PEDESTAL_ITEM = ShadowsThings.ITEMS.register(
            "resonant_pedestal", () -> new BlockItem(RESONANT_PEDESTAL.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(RESONANT_PEDESTAL_ITEM.get())
        );
    }
}
