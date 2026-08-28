package com.lucab.shadows_things.content.block.resonant.resonant_altar;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.resonant.resonant_pedestal.ResonantPedestalBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ResonantAltarRegistry {
    public static final DeferredBlock<ResonantAltarBlock> RESONANT_ALTAR = ShadowsThings.BLOCKS.register(
            "resonant_altar", () -> new ResonantAltarBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantAltarBlockEntity>> RESONANT_ALTAR_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "resonant_altar", () -> BlockEntityType.Builder.of(ResonantAltarBlockEntity::new,
                    RESONANT_ALTAR.get()).build(null));

    public static DeferredItem<BlockItem> RESONANT_ALTAR_ITEM = ShadowsThings.ITEMS.register(
            "resonant_altar", () -> new BlockItem(RESONANT_ALTAR.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(RESONANT_ALTAR_ITEM.get())
        );
    }
}
