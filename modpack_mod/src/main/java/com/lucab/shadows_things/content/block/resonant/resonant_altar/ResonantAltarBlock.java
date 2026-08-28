package com.lucab.shadows_things.content.block.resonant.resonant_altar;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ResonantAltarBlock extends BaseEntityBlock {
    public static final MapCodec<ResonantAltarBlock> CODEC = simpleCodec(ResonantAltarBlock::new);

    public ResonantAltarBlock(Properties properties) {
        super(properties);
    }

    public ResonantAltarBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.AMETHYST)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ResonantAltarBlockEntity altar))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!level.isClientSide) {
            if (!player.isShiftKeyDown()) {
                ItemStack itemInHand = player.getItemInHand(hand);
                if (!itemInHand.isEmpty()) {
                    if (altar.insertItem(itemInHand))
                        itemInHand.shrink(player.getAbilities().instabuild ? 0 : 1);
                }
            } else {
                ItemStack retrievedItem = altar.removeItems();
                if (!player.getInventory().add(retrievedItem))
                    player.drop(retrievedItem, false);
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ResonantAltarBlockEntity altar) {
                altar.unbindAndNotifyAllPedestals();
                for (int i = 0; i < altar.inventory.getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), altar.inventory.getStackInSlot(i));
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ResonantAltarRegistry.RESONANT_ALTAR_ENTITY.get(), ResonantAltarBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantAltarBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
