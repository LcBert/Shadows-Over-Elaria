package com.lucab.shadows_things.block;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;
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

    // Stone Pebble
    public static final DeferredBlock<Block> STONE_PEBBLE = ShadowsThings.BLOCKS.register("stone_pebble",
            () -> new Block(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.STONE)
                    .strength(0.3f)
                    .noOcclusion()) {
                protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                        CollisionContext context) {
                    return Block.box(0, 0, 0, 16, 1, 16);
                };
            });

    public static final DeferredItem<BlockItem> STONE_PEBBLE_ITEM = ShadowsThings.ITEMS.register("stone_pebble",
            () -> new BlockItem(STONE_PEBBLE.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(REPAIR_TABLE_ITEM.get()),
                new ItemStack(STONE_PEBBLE.get()));
    }
}
