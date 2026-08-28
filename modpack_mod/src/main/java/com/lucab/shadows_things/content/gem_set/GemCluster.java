package com.lucab.shadows_things.content.gem_set;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.worldgen.deep_cave.DeepCaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GemCluster extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final float HEIGHT = 16;
    private final float AABB_OFFSET = 3;

    protected final VoxelShape northAabb;
    protected final VoxelShape southAabb;
    protected final VoxelShape eastAabb;
    protected final VoxelShape westAabb;
    protected final VoxelShape upAabb;
    protected final VoxelShape downAabb;

    public GemCluster() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .sound(SoundType.AMETHYST)
                .strength(10.0F, 1.5F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .noLootTable()
        );
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));

        this.upAabb = Block.box(
                AABB_OFFSET, 0.0, AABB_OFFSET, (16.0F - AABB_OFFSET), HEIGHT, (16.0F - AABB_OFFSET)
        );
        this.downAabb = Block.box(
                AABB_OFFSET, (16.0F - HEIGHT), AABB_OFFSET, (16.0F - AABB_OFFSET), 16.0, (16.0F - AABB_OFFSET)
        );
        this.northAabb = Block.box(
                AABB_OFFSET, AABB_OFFSET, (16.0F - HEIGHT), (16.0F - AABB_OFFSET), (16.0F - AABB_OFFSET), 16.0
        );
        this.southAabb = Block.box(
                AABB_OFFSET, AABB_OFFSET, 0.0, (16.0F - AABB_OFFSET), (16.0F - AABB_OFFSET), HEIGHT
        );
        this.eastAabb = Block.box(
                0.0, AABB_OFFSET, AABB_OFFSET, HEIGHT, (16.0F - AABB_OFFSET), (16.0F - AABB_OFFSET)
        );
        this.westAabb = Block.box(
                (16.0F - HEIGHT), AABB_OFFSET, AABB_OFFSET, 16.0, (16.0F - AABB_OFFSET), (16.0F - AABB_OFFSET)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor levelAccessor = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case NORTH -> this.northAabb;
            case SOUTH -> this.southAabb;
            case EAST -> this.eastAabb;
            case WEST -> this.westAabb;
            case DOWN -> this.downAabb;
            default -> this.upAabb;
        };
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && willHarvest) {
            List<int[]> heights = DeepCaveData.getHeights();
            int clusterTier = 1;
            for (int i = 0; i < heights.size(); i++) {
                int[] height = heights.get(i);
                if (pos.getY() > height[0] && pos.getY() < height[1]) {
                    clusterTier = i + 1;
                    break;
                }
            }
            ItemStack gemStack = GemItem.getRandomGem(clusterTier);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, gemStack);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    public static final DeferredBlock<GemCluster> GEM_CLUSTER = ShadowsThings.BLOCKS.register("gem_cluster",
            GemCluster::new);

    public static final DeferredItem<BlockItem> GEM_CLUSTER_ITEM = ShadowsThings.ITEMS.register("gem_cluster",
            () -> new BlockItem(GEM_CLUSTER.get(), new Item.Properties()));


    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(GEM_CLUSTER_ITEM.get())
        );
    }
}
