package com.lucab.shadows_things.content.block.deep_cave_portal_block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class DeepCavePortalBlock extends BaseEntityBlock {
    public static final MapCodec<DeepCavePortalBlock> CODEC = simpleCodec(DeepCavePortalBlock::new);

    public DeepCavePortalBlock(Properties properties) {
        super(properties);
    }

    public DeepCavePortalBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(-1.0f)
                .noLootTable()
        );
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, DeepCavePortalRegister.DEEP_CAVE_PORTAL_ENTITY.get(), DeepCavePortalEntity::clientTick);
        } else {
            return createTickerHelper(blockEntityType, DeepCavePortalRegister.DEEP_CAVE_PORTAL_ENTITY.get(), DeepCavePortalEntity::serverTick);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeepCavePortalEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}