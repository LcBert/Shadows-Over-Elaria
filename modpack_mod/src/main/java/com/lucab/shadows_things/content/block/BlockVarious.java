package com.lucab.shadows_things.content.block;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BlockVarious {
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
}
