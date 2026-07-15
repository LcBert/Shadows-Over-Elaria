package com.lucab.shadows_things.block;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.block.deep_cave_portal_block.DeepCavePortalBlock;
import com.lucab.shadows_things.block.deep_cave_portal_block.DeepCavePortalEntity;
import com.lucab.shadows_things.block.oven.OvenBlock;
import com.lucab.shadows_things.block.oven.OvenBlockEntity;
import com.lucab.shadows_things.block.repair_table.RepairTable;
import com.lucab.shadows_things.block.repair_table.RepairTableEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class BlocksRegister {
    // Repair Table
    public static final DeferredBlock<RepairTable> REPAIR_TABLE = ShadowsThings.BLOCKS.register("repair_table",
            () -> new RepairTable());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepairTableEntity>> REPAIR_TABLE_ENTITY = ShadowsThings.BLOCK_ENTITIES
            .register("repair_table",
                    () -> BlockEntityType.Builder.of(RepairTableEntity::new,
                            REPAIR_TABLE.get()).build(null));

    public static final DeferredItem<BlockItem> REPAIR_TABLE_ITEM = ShadowsThings.ITEMS.register("repair_table",
            () -> new BlockItem(REPAIR_TABLE.get(), new Item.Properties()));

    // Oven
    public static final DeferredBlock<OvenBlock> OVEN_BlOCK = ShadowsThings.BLOCKS.register(
            "oven", () -> new OvenBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OvenBlockEntity>> OVEN_BLOCK_ENTITY = ShadowsThings.BLOCK_ENTITIES
            .register("oven",
                    () -> BlockEntityType.Builder.of(OvenBlockEntity::new,
                            OVEN_BlOCK.get()).build(null));

    public static final DeferredItem<BlockItem> OVEN_BLOCK_ITEM = ShadowsThings.ITEMS.register("oven",
            () -> new BlockItem(OVEN_BlOCK.get(), new Item.Properties())
    );

    // Deep Cave Portal Block
    public static final DeferredBlock<DeepCavePortalBlock> DEEP_CAVE_PORTAL = ShadowsThings.BLOCKS.register(
            "deep_cave_portal", () -> new DeepCavePortalBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepCavePortalEntity>> DEEP_CAVE_PORTAL_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "deep_cave_portal", () -> BlockEntityType.Builder.of(DeepCavePortalEntity::new,
                    DEEP_CAVE_PORTAL.get()).build(null));

    public static final DeferredItem<BlockItem> DEEP_CAVE_PORTAL_ITEM = ShadowsThings.ITEMS.register(
            "deep_cave_portal", () -> new BlockItem(DEEP_CAVE_PORTAL.get(), new Item.Properties()));

    // Stone Pebble
    public static final DeferredBlock<Block> FLINT_PEBBLE = ShadowsThings.BLOCKS.register("flint_pebble",
            () -> new Block(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .strength(0.3f)
                    .noOcclusion()) {
                protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    return Block.box(0, 0, 0, 16, 1, 16);
                }
            });

    // Stick block
    public static final DeferredBlock<Block> STICK_BLOCK = ShadowsThings.BLOCKS.register("stick_block",
            () -> new Block(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(0.3f)
                    .noOcclusion()) {
                protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    return Block.box(0, 0, 0, 16, 1, 16);
                }
            });

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

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(REPAIR_TABLE_ITEM.get()),
                new ItemStack(OVEN_BLOCK_ITEM.get()),
                new ItemStack(DEEP_CAVE_PORTAL_ITEM.get()),
                new ItemStack(SILVER_ORE_ITEM.get()),
                new ItemStack(RAW_SILVER.get()),
                new ItemStack(SILVER_INGOT.get())
        );
    }
}
