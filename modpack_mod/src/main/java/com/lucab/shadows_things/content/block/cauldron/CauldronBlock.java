package com.lucab.shadows_things.content.block.cauldron;

import com.lucab.shadows_things.content.item.GlassBottles;
import com.lucab.shadows_things.menus.CauldronMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronBlock extends BaseEntityBlock {
    public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CauldronBlock(Properties properties) {
        super(properties);
        registerDefaultState();
    }

    public CauldronBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.5f)
                .noOcclusion()
                .requiresCorrectToolForDrops()
        );
        registerDefaultState();
    }

    private void registerDefaultState() {
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CauldronBlockEntity cauldronEntity) {
                if (stack.is(Items.WATER_BUCKET)) {
                    if (cauldronEntity.insertWaterBucket()) {
                        if (!player.isCreative()) {
                            player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
                        }
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                } else if (stack.is(Items.BUCKET)) {
                    if (cauldronEntity.extractWaterBucket()) {
                        if (!player.isCreative()) {
                            if (stack.getCount() == 1) {
                                player.setItemInHand(hand, Items.WATER_BUCKET.getDefaultInstance());
                            } else {
                                stack.shrink(1);
                                if (!player.getInventory().add(Items.WATER_BUCKET.getDefaultInstance()))
                                    player.drop(Items.WATER_BUCKET.getDefaultInstance(), false);
                            }
                        }
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                } else if (stack.is(Items.GLASS_BOTTLE)
                        || stack.is(GlassBottles.SPLASH_GLASS_BOTTLE)
                        || stack.is(GlassBottles.LINGERING_GLASS_BOTTLE)) {
                    ItemStack potionBottle = cauldronEntity.getBottleWithPotion(stack);
                    if (!potionBottle.isEmpty()) {
                        if (stack.getCount() == 1 && !player.isCreative()) {
                            player.setItemInHand(hand, potionBottle);
                        } else {
                            if (!player.isCreative()) stack.shrink(1);
                            if (!player.getInventory().add(potionBottle))
                                player.drop(potionBottle, false);
                        }
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new CauldronMenu(containerId, playerInventory, cauldronEntity),
                            Component.translatable(state.getBlock().getDescriptionId())
                    ), pos);
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CauldronBlockEntity cauldronBlockEntity) {
                IItemHandler inventory = cauldronBlockEntity.getInventoryHandler();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, CauldronRegister.CAULDRON_BLOCK_ENTITY.get(), CauldronBlockEntity::tick);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.875, 0.125, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 0.125, 0.125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0, 1, 0.125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 1, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0, 1, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0.875, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0.125, 0.125, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.125, 0.125, 1, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.125, 0.125, 0.875, 0.1875, 0.875), BooleanOp.OR);

        return shape;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
