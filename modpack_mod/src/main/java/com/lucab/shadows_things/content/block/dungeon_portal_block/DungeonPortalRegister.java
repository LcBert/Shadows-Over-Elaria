package com.lucab.shadows_things.content.block.dungeon_portal_block;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class DungeonPortalRegister {
    public static final DeferredBlock<DungeonPortalBlock> DUNGEON_PORTAL = ShadowsThings.BLOCKS.register(
            "dungeon_portal", () -> new DungeonPortalBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DungeonPortalEntity>> DUNGEON_PORTAL_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "dungeon_portal", () -> BlockEntityType.Builder.of(DungeonPortalEntity::new,
                    DUNGEON_PORTAL.get()).build(null));

    public static final DeferredItem<BlockItem> DUNGEON_PORTAL_ITEM = ShadowsThings.ITEMS.register(
            "dungeon_portal", () -> new BlockItem(DUNGEON_PORTAL.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(new ItemStack(DUNGEON_PORTAL.get()));
    }
}
