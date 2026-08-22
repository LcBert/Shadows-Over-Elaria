package com.lucab.shadows_things.content.block.deep_cave_portal_block;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.Utils;
import com.lucab.shadows_things.worldgen.DeepCave.DeepCaveDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
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
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeepCavePortalEntity extends BlockEntity {
    protected static final int ENTRANCE_RADIUS = 3;
    private static final int ENTRANCE_TICK = 300;
    private static final int ENTRANCE_EFFECTS_TICK = 50;
    private static final int ENTRANCE_FIRST_MESSAGE_TICK = 50;
    private static final int ENTRANCE_SECOND_MESSAGE_TICK = 150;

    private int tickCount = 0;

    public DeepCavePortalEntity(BlockPos pos, BlockState state) {
        super(DeepCavePortalRegister.DEEP_CAVE_PORTAL_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DeepCavePortalEntity be) {
        be.tickCount++;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1;
        double centerZ = pos.getZ() + 0.5;

        // Circle
        if (be.tickCount % 5 == 0) {
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

        // Spiral / Vortex effect
        int particlesPerTick = 3;
        for (int i = 0; i < particlesPerTick; i++) {
            double angle = (be.tickCount * 0.2) + (i * (Math.PI / 2));
            double radius = ENTRANCE_RADIUS * (1.0 - ((be.tickCount % 40) / 40.0)); // inward pull effect
            if (radius < 0.1) radius = ENTRANCE_RADIUS;

            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            double y = centerY + ((be.tickCount % 40) * 0.1); // moves upward

            level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeepCavePortalEntity be) {
        if (level == null) return;

        List<Player> nearbyPlayers = be.getNearbyEntities();
        if (nearbyPlayers.isEmpty())
            be.tickCount = 0;
        else
            be.tickCount++;

        if (be.tickCount == 1) {
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (be.tickCount > 0 && be.tickCount % 60 == 0) {
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        // Add Effects
        if (be.tickCount >= ENTRANCE_EFFECTS_TICK) {
            nearbyPlayers.forEach(player -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, false, false));
            });
        }

        // Show messages
        if (be.tickCount == ENTRANCE_FIRST_MESSAGE_TICK) {
            nearbyPlayers.forEach(player -> {
                Utils.sendTitleMessage(player,
                        Component.literal("Allineamento al portale"),
                        Component.literal("La tua essenza si sta allineando al portale")
                );
            });
        }

        if (be.tickCount == ENTRANCE_SECOND_MESSAGE_TICK) {
            nearbyPlayers.forEach(player -> {
                Utils.sendTitleMessage(player,
                        Component.literal("Portale aperto"),
                        Component.literal("Preparati ad entrare")
                );
            });
        }

        if (be.tickCount >= ENTRANCE_TICK) {
            be.teleportPlayers(nearbyPlayers);
            be.tickCount = ENTRANCE_TICK - 20;
        }
    }

    private List<Player> getNearbyEntities() {
        BlockPos pos = this.getBlockPos();

        // Define AABB starting from the top of the block, going up by 3 blocks, and bounded horizontally by ENTRANCE_RADIUS
        AABB search_box = new AABB(
                pos.getX() - ENTRANCE_RADIUS, pos.getY() + 1, pos.getZ() - ENTRANCE_RADIUS,
                pos.getX() + 1 + ENTRANCE_RADIUS, pos.getY() + 4, pos.getZ() + 1 + ENTRANCE_RADIUS
        );

        double radiusSq = ENTRANCE_RADIUS * ENTRANCE_RADIUS;
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        return level.getEntities(
                EntityTypeTest.forClass(Player.class),
                search_box,
                player -> {
                    double playerY = player.getY();
                    if (playerY < pos.getY() + 1 || playerY > pos.getY() + 4) {
                        return false;
                    }

                    double dx = player.getX() - centerX;
                    double dz = player.getZ() - centerZ;
                    return (dx * dx + dz * dz) <= radiusSq;
                }
        );
    }

    private void teleportPlayers(List<Player> players) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel targetLevel = server.getLevel(DeepCaveDimension.DEEP_CAVE_LEVEL_KEY);
        if (targetLevel == null) return;

        int x = this.worldPosition.getX() + ThreadLocalRandom.current().nextInt(-1000, 1000);
        int z = this.worldPosition.getZ() + ThreadLocalRandom.current().nextInt(-1000, 1000);
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
}