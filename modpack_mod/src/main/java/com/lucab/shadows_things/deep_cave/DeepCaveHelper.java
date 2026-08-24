package com.lucab.shadows_things.deep_cave;

import com.lucab.shadows_things.content.item.EscapeRope;
import com.lucab.shadows_things.rpg.classes.ClassManager;
import com.lucab.shadows_things.worldgen.DeepCave.DeepCaveData;
import com.lucab.shadows_things.worldgen.DeepCave.DeepCaveDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeepCaveHelper {
    public static void teleportPlayersIntoCave(Level level, BlockPos searchPos, Direction blockDir, List<Player> players) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel targetLevel = server.getLevel(DeepCaveDimension.DEEP_CAVE_LEVEL_KEY);
        if (targetLevel == null) return;

        int minTier = players.stream()
                .mapToInt(ClassManager::getTier)
                .filter(tier -> tier >= 1 && tier <= 5)
                .min()
                .orElse(1);

        int[] layerBounds = DeepCaveData.getLayerHeight(minTier);
        int targetMinY = Math.min(layerBounds[1], layerBounds[0]);
        int targetMaxY = Math.max(layerBounds[1], layerBounds[0]);

        int x = searchPos.getX() + ThreadLocalRandom.current().nextInt(-1000, 1000);
        int z = searchPos.getZ() + ThreadLocalRandom.current().nextInt(-1000, 1000);

        BlockPos targetPos = findSafePosition(targetLevel, x, z, targetMinY, targetMaxY);
        if (targetPos == null) return;

        for (Player player : players) {
            player.getInventory().add(new ItemStack(EscapeRope.ESCAPE_ROPE.get()));
            DeepCavePlayerAttachment playerData = player.getData(DeepCavePlayerAttachment.DEEP_CAVE_ATTACHMENT);
            playerData.setPortalPos(searchPos);
            playerData.setPortalDir(blockDir);
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

    private static BlockPos findSafePosition(Level level, int x, int z, int minY, int maxY) {
        level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);

        int clampedMinY = Math.max(level.getMinBuildHeight(), minY);
        int clampedMaxY = Math.min(level.getMaxBuildHeight() - 1, maxY);

        for (int y = clampedMinY; y <= clampedMaxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSafe(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSafe(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() &&
                level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty() &&
                !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }

    public static void exitPlayer(Level level, Player player) {
        MinecraftServer server = level.getServer();
        if (server == null) return;

        ServerLevel targetLevel = server.getLevel(Level.OVERWORLD);
        if (targetLevel == null) return;

        DeepCavePlayerAttachment data = player.getData(DeepCavePlayerAttachment.DEEP_CAVE_ATTACHMENT);

        BlockPos portalPos = data.getPortalPos();
        Direction portalDir = data.getPortalDir();

        if (portalPos == null) portalPos = targetLevel.getSharedSpawnPos();

        BlockPos targetPos = portalPos.relative(portalDir, 8);
        targetPos = targetPos.above();

        float xRot = portalDir.toYRot();
        float yRot = 0.0F;

        if (player instanceof ServerPlayer serverPlayer) {
            DimensionTransition transition = new DimensionTransition(
                    targetLevel,
                    targetPos.getCenter(),
                    Vec3.ZERO,
                    xRot,
                    yRot,
                    DimensionTransition.DO_NOTHING
            );
            serverPlayer.changeDimension(transition);
            player.playSound(SoundEvents.PORTAL_TRAVEL, 1.0F, 1.0F);
        }

        // Remove escape rope from player inventory
        if (!player.getAbilities().instabuild) {
            player.getInventory().items.forEach(item -> {
                if (item.is(EscapeRope.ESCAPE_ROPE.get())) {
                    item.shrink(1);
                }
            });
        }
    }
}
