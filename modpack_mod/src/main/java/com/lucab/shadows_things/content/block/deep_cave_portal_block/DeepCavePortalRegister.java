package com.lucab.shadows_things.content.block.deep_cave_portal_block;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class DeepCavePortalRegister {
    public static final DeferredBlock<DeepCavePortalBlock> DEEP_CAVE_PORTAL = ShadowsThings.BLOCKS.register(
            "deep_cave_portal", () -> new DeepCavePortalBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepCavePortalEntity>> DEEP_CAVE_PORTAL_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "deep_cave_portal", () -> BlockEntityType.Builder.of(DeepCavePortalEntity::new,
                    DEEP_CAVE_PORTAL.get()).build(null));

    public static final DeferredItem<BlockItem> DEEP_CAVE_PORTAL_ITEM = ShadowsThings.ITEMS.register(
            "deep_cave_portal", () -> new BlockItem(DEEP_CAVE_PORTAL.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(DEEP_CAVE_PORTAL_ITEM.get())
        );
    }
}
