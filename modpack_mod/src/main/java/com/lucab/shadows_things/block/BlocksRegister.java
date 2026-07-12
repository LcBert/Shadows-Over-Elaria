package com.lucab.shadows_things.block;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;
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

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(REPAIR_TABLE_ITEM.get()),
                new ItemStack(OVEN_BLOCK_ITEM.get())
        );
    }
}
