package com.lucab.shadows_things.spawns;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class SpawnsHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        if (level.getGameTime() % 20 != 0) return;

        for (Map.Entry<String, SpawnsDataReader.SpawnData> entry : ShadowsThings.SPAWNS_READER.getSpawns().entrySet()) {
            SpawnsDataReader.SpawnData data = entry.getValue();
            SpawnsDataReader.SpawnProperties props = data.spawnProperties();

            if (!props.spawnOnPeaceful() && level.getDifficulty() == Difficulty.PEACEFUL) continue;
            if (props.spawnOnlyOnNight() && !level.isNight()) continue;
            if (ThreadLocalRandom.current().nextFloat() > props.spawnChance()) continue;
            if (!checkDimension(level, data.dimensions())) continue;

            EntityType<?> entityType = data.entityType();

            AABB searchArea = player.getBoundingBox().inflate(props.spawnRadius());
            int nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea, e -> e.getType() == entityType).size();
            if (nearbyEntities >= props.spawnCap()) continue;

            int playerX = player.getBlockX();
            int playerY = player.getBlockY();
            int playerZ = player.getBlockZ();
            int spawnRadius = props.spawnRadius();
            int safeRadius = Math.min(props.safeRadius(), spawnRadius - 1);

            for (int tries = 1; tries <= 20; tries++) {
                int spawnX = ThreadLocalRandom.current().nextInt(-spawnRadius, spawnRadius + 1) + playerX;
                int spawnZ = ThreadLocalRandom.current().nextInt(-spawnRadius, spawnRadius + 1) + playerZ;

                int minY = Math.max(props.spawnHeight()[0] + playerY, props.spawnY()[0]);
                int maxY = Math.min(props.spawnHeight()[1] + playerY, props.spawnY()[1]);
                if (minY > maxY) continue;

                BlockPos targetPos = findValidFloor(level, entityType, spawnX, spawnZ, playerY, minY, maxY);
                if (targetPos == null) continue;

                if (player.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < safeRadius * safeRadius)
                    continue;

                if (!checkBiome(level, targetPos, data.biomes())) continue;
                if (!checkLight(level, targetPos, data.light())) continue;

                int minCount = data.count()[0];
                int maxCount = data.count()[1];
                int spawnCount = ThreadLocalRandom.current().nextInt(minCount, maxCount + 1);

                boolean anySpawned = false;
                for (int i = 0; i < spawnCount; i++) {
                    Entity spawnedEntity = entityType.create(level);
                    if (spawnedEntity == null) break;

                    BlockPos memberPos = (i == 0) ? targetPos : findNearbyPosition(level, entityType, targetPos, 3);
                    if (memberPos == null) continue;

                    float randomYaw = ThreadLocalRandom.current().nextFloat() * 360.0F;
                    spawnedEntity.moveTo(
                            memberPos.getX() + 0.5,
                            memberPos.getY(),
                            memberPos.getZ() + 0.5,
                            randomYaw,
                            0.0F);

                    if (spawnedEntity instanceof Mob mob) {
                        DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(memberPos);
                        EventHooks.finalizeMobSpawn(mob, serverLevel, difficulty, MobSpawnType.EVENT, null);
                    }

                    if (serverLevel.addFreshEntity(spawnedEntity)) {
                        anySpawned = true;
                    }
                }

                if (anySpawned) break;
            }
        }
    }

    private static BlockPos findValidFloor(Level level, EntityType<?> entityType, int x, int z, int originY, int minY, int maxY) {
        int clampedMinY = Math.max(minY, level.getMinBuildHeight() + 1);
        int clampedMaxY = Math.min(maxY, level.getMaxBuildHeight() - 2);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 1. SCENARIO ALL'APERTO (Heightmap veloce)
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (surfaceY >= clampedMinY && surfaceY <= clampedMaxY) {
            pos.set(x, surfaceY, z);
            // Se il punto di superficie vede direttamente il cielo, siamo all'aperto
            if (level.canSeeSky(pos)) {
                if (isValidSpawn(level, entityType, pos)) {
                    return pos.immutable();
                }
            }
        }

        // 2. SCENARIO CAVERNA / SOTTERRANEO / COPERTO (Center-Out da playerY)
        int maxDelta = Math.max(originY - clampedMinY, clampedMaxY - originY);

        for (int step = 0; step <= maxDelta; step++) {
            // Controlla verso il basso
            int yDown = originY - step;
            if (yDown >= clampedMinY && yDown <= clampedMaxY) {
                pos.set(x, yDown, z);
                if (isValidSpawn(level, entityType, pos)) {
                    return pos.immutable();
                }
            }

            // Controlla verso l'alto
            if (step > 0) {
                int yUp = originY + step;
                if (yUp >= clampedMinY && yUp <= clampedMaxY) {
                    pos.set(x, yUp, z);
                    if (isValidSpawn(level, entityType, pos)) {
                        return pos.immutable();
                    }
                }
            }
        }

        return null;
    }

    private static boolean isValidSpawn(Level level, EntityType<?> entityType, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        if (!belowState.getFluidState().isEmpty() || !level.getFluidState(pos).isEmpty()) {
            return false;
        }

        float entityHeight = entityType.getDimensions().height();
        int heightBlocks = Math.max(1, (int) Math.ceil(entityHeight));

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int h = 0; h < heightBlocks; h++) {
            checkPos.set(pos.getX(), pos.getY() + h, pos.getZ());
            BlockState state = level.getBlockState(checkPos);

            if (state.isSuffocating(level, checkPos)) {
                return false;
            }
        }

        AABB mobBox = entityType.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return level.noCollision(mobBox);
    }

    private static BlockPos findNearbyPosition(Level level, EntityType<?> entityType, BlockPos origin, int offsetRadius) {
        for (int i = 0; i < 5; i++) {
            int ox = origin.getX() + ThreadLocalRandom.current().nextInt(-offsetRadius, offsetRadius + 1);
            int oz = origin.getZ() + ThreadLocalRandom.current().nextInt(-offsetRadius, offsetRadius + 1);

            for (int dy : new int[]{0, -1, 1, -2, 2}) {
                BlockPos testPos = new BlockPos(ox, origin.getY() + dy, oz);
                if (isValidSpawn(level, entityType, testPos)) {
                    return testPos;
                }
            }
        }
        return null;
    }

    private static boolean checkDimension(Level level, Set<ResourceKey<Level>> dimensions) {
        if (dimensions == null || dimensions.isEmpty()) return true;
        return dimensions.contains(level.dimension());
    }

    private static boolean checkBiome(Level level, BlockPos pos, List<SpawnsDataReader.BiomeFilter> biomes) {
        if (biomes == null || biomes.isEmpty()) return true;

        Holder<Biome> currentBiome = level.getBiome(pos);

        for (SpawnsDataReader.BiomeFilter filter : biomes) {
            if (filter.tag().isPresent() && currentBiome.is(filter.tag().get())) {
                return true;
            }
            if (filter.biomeId().isPresent()) {
                boolean matches = currentBiome.unwrapKey()
                        .map(key -> key.location().equals(filter.biomeId().get()))
                        .orElse(false);
                if (matches) return true;
            }
        }

        return false;
    }

    private static boolean checkLight(Level level, BlockPos pos, int[] light) {
        if (light == null || light.length < 2) return false;
        int currentLight = level.getMaxLocalRawBrightness(pos);
        return currentLight >= light[0] && currentLight <= light[1];
    }
}
