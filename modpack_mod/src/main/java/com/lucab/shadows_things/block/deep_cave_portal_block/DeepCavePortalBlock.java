package com.lucab.shadows_things.block.deep_cave_portal_block;

import com.lucab.shadows_things.block.BlocksRegister;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
        );
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        DustParticleOptions particle = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 1.0f), 1);
        int points = 32;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double x = pos.getX() + 0.5 + Math.cos(angle) * DeepCavePortalEntity.ENTRANCE_RADIUS;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * DeepCavePortalEntity.ENTRANCE_RADIUS;
            double y = pos.getY() + 1.2;
            level.addParticle(particle, x, y, z, 0.5, 0.0, 0.0);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlocksRegister.DEEP_CAVE_PORTAL_ENTITY.get(), DeepCavePortalEntity::tick);
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
