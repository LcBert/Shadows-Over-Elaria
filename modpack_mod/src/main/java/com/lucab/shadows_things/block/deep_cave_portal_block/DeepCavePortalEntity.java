package com.lucab.shadows_things.block.deep_cave_portal_block;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.block.BlocksRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeepCavePortalEntity extends BlockEntity {
    private static final ResourceKey<Level> DIMENSION_KEY = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave"));

    protected static final int ENTRANCE_RADIUS = 3;
    private static final int ENTRANCE_TICK = 100;

    private int tickCount = 0;

    public DeepCavePortalEntity(BlockPos pos, BlockState state) {
        super(BlocksRegister.DEEP_CAVE_PORTAL_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DeepCavePortalEntity be) {
        if (level == null || level.isClientSide) return;
        List<Player> nearbyPlayers = be.getNearbyEntities();
        if (nearbyPlayers.isEmpty()) be.tickCount = 0;
        else be.tickCount++;

        if (be.tickCount == 1) {
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (be.tickCount > 0 && be.tickCount % 60 == 0) {
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (be.tickCount >= ENTRANCE_TICK / 2) {
            nearbyPlayers.forEach(player -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, false, false));
            });
        }

        if (be.tickCount >= ENTRANCE_TICK) {
            be.teleportPlayers(nearbyPlayers);
            be.tickCount = ENTRANCE_TICK - 20;
        }

    }

    private List<Player> getNearbyEntities() {
        AABB search_box = new AABB(this.getBlockPos()).inflate(ENTRANCE_RADIUS);
        double radiusSq = ENTRANCE_RADIUS * ENTRANCE_RADIUS;

        double centerX = this.getBlockPos().getX() + 0.5;
        double centerY = this.getBlockPos().getY() + 0.5;
        double centerZ = this.getBlockPos().getZ() + 0.5;

        return level.getEntities(
                EntityTypeTest.forClass(Player.class),
                search_box,
                player -> player.distanceToSqr(centerX, centerY, centerZ) <= radiusSq
        );
    }

    private void teleportPlayers(List<Player> players) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel targetLevel = server.getLevel(DIMENSION_KEY);
        if (targetLevel == null) return;

        int x = ThreadLocalRandom.current().nextInt(-1000, 1000);
        int z = ThreadLocalRandom.current().nextInt(-1000, 1000);
        int y = getBlockPos().getY();

        BlockPos targetPos = findSafePosition(targetLevel, new BlockPos(x, y, z));

        if (targetPos == null) return;

        for (Player player : players) {
            if (player instanceof ServerPlayer serverPlayer) {
                DimensionTransition transition = new DimensionTransition(
                        targetLevel,
                        targetPos.getCenter(),
                        Vec3.ZERO,
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot(),
                        DimensionTransition.DO_NOTHING
                );
                serverPlayer.changeDimension(transition);
                player.playSound(SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.0F);
            }
        }
    }

    private BlockPos findSafePosition(ServerLevel level, BlockPos targetPos) {
        int x = targetPos.getX();
        int z = targetPos.getZ();

        level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() - 1; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSafe(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean isSafe(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() &&
                level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty() &&
                !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}
