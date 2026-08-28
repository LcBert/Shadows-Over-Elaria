package com.lucab.shadows_things.worldgen.features_type;

import com.lucab.shadows_things.content.gem_set.GemCluster;
import com.lucab.shadows_things.content.gem_set.GemItem;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.*;

public class ClusterFeatureType extends Feature<NoneFeatureConfiguration> {
    public ClusterFeatureType(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int heightSpan = maxY - minY + 1;

        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos wallPos = new BlockPos.MutableBlockPos();

        int columnAttempts = 16;
        for (int i = 0; i < columnAttempts; i++) {
            int localX = origin.getX() + random.nextInt(16);
            int localZ = origin.getZ() + random.nextInt(16);
            int startY = minY + random.nextInt(heightSpan);

            if (tryPlaceAt(level, random, searchPos.set(localX, startY, localZ), wallPos)) return true;

            int maxOffset = Math.max(startY - minY, maxY - startY);

            for (int dy = 1; dy <= maxOffset; dy++) {
                // Check Upward (+dy)
                int upY = startY + dy;
                if (upY <= maxY) {
                    if (tryPlaceAt(level, random, searchPos.set(localX, upY, localZ), wallPos)) return true;
                }

                // Check Downward (-dy)
                int downY = startY - dy;
                if (downY >= minY) {
                    if (tryPlaceAt(level, random, searchPos.set(localX, downY, localZ), wallPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryPlaceAt(WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos placePos, BlockPos.MutableBlockPos wallPos) {
        BlockState currentState = level.getBlockState(placePos);
        if (!currentState.canBeReplaced()) {
            return false;
        }

        List<Direction> directions = new ArrayList<>(Arrays.stream(Direction.values()).toList());
        Collections.shuffle(directions, new Random(random.nextLong()));
        for (Direction targetFacing : directions) {
            Direction wallDirection = targetFacing.getOpposite();
            wallPos.setWithOffset(placePos, wallDirection);
            BlockState wallState = level.getBlockState(wallPos);

            if (wallState.isFaceSturdy(level, wallPos, targetFacing, SupportType.FULL)) {
                FluidState fluidState = level.getFluidState(placePos);
                boolean isWaterlogged = fluidState.getType() == Fluids.WATER;

                BlockState clusterState = GemCluster.GEM_CLUSTER.get().defaultBlockState().setValue(BlockStateProperties.FACING, targetFacing).setValue(BlockStateProperties.WATERLOGGED, isWaterlogged);

                if (clusterState.canSurvive(level, placePos)) {
                    setBlock(level, placePos, clusterState);
                    return true;
                }
            }
        }

        return false;
    }
}
