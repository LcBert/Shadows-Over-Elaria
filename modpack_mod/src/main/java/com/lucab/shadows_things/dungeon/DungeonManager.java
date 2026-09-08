package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.dungeon.spawns.DungeonSpawnConfig;
import com.lucab.shadows_things.dungeon.spawns.DungeonSpawnEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DungeonManager {
    public enum DungeonType {
        JUNGLE("jungle", new DungeonSpawnConfig(4, 8, List.of(
                new DungeonSpawnEntry("minecraft:zombie", 1),
                new DungeonSpawnEntry("minecraft:skeleton", 1),
                new DungeonSpawnEntry("minecraft:wither_skeleton", 1)
        )));

        private final ResourceKey<Structure> key;
        private final DungeonSpawnConfig spawnConfig;

        DungeonType(String path, DungeonSpawnConfig spawnConfig) {
            this.key = ResourceKey.create(
                    Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "dungeons/" + path)
            );
            this.spawnConfig = spawnConfig;
        }

        public ResourceKey<Structure> getKey() {
            return key;
        }

        public DungeonSpawnConfig getSpawnConfig() {
            return spawnConfig;
        }
    }

    protected static final int DUNGEON_SIZE = 200;
    private static final int DUNGEON_OFFSET = 512;

    public static final ResourceKey<Level> DUNGEON_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "dungeon")
    );

    private static MinecraftServer server;
    private static final Map<Long, DungeonInstance> DUNGEON_INSTANCES = new HashMap<>();

    public static void initializeServer(MinecraftServer server) {
        DungeonManager.server = server;
    }

    public static void clearServer() {
        DUNGEON_INSTANCES.clear();
        server = null;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static ServerLevel getDungeonLevel() {
        if (server == null) return null;
        return server.getLevel(DUNGEON_LEVEL_KEY);
    }

    public static DungeonInstance createDungeonInstance() {
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return null;

        BlockPos spawnPos;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long id;
        do {
            int xOffset = random.nextInt(2049) - 1024;
            int zOffset = random.nextInt(2049) - 1024;
            spawnPos = new BlockPos(DUNGEON_OFFSET * xOffset, 200, DUNGEON_OFFSET * zOffset);
            id = spawnPos.asLong();
        } while (DUNGEON_INSTANCES.containsKey(id));

        DungeonInstance instance = new DungeonInstance(id, spawnPos);
        DUNGEON_INSTANCES.put(instance.getId(), instance);
        return instance;
    }

    public static void removeDungeon(long id) {
        DUNGEON_INSTANCES.remove(id);
    }

    public static DungeonInstance getDungeon(long id) {
        return DUNGEON_INSTANCES.get(id);
    }

    public static Map<Long, DungeonInstance> getDungeonInstances() {
        return Collections.unmodifiableMap(DUNGEON_INSTANCES);
    }

    public static Map<Long, DungeonInstance> getInternalMap() {
        return DUNGEON_INSTANCES;
    }

    public static DungeonInstance getInstanceForPlayer(Player player) {
        for (DungeonInstance instance : DUNGEON_INSTANCES.values()) {
            if (instance.getPlayers().contains(player.getUUID())) {
                return instance;
            }
        }
        return null;
    }

    public static boolean isPlayerInDungeon(Player player) {
        return getInstanceForPlayer(player) != null;
    }

    public static List<Boolean> exitPlayers(List<UUID> players) {
        List<Boolean> result = new ArrayList<>();
        for (UUID player : players) {
            result.add(exitPlayer(player));
        }
        return result;
    }

    public static boolean exitPlayer(UUID playerUuid) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return false;

        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        if (player == null) return false;

        if (!isPlayerInDungeon(player)) return false;

        DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);
        if (instance == null) return false;

        DungeonPlayerData playerData = player.getData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

        BlockPos portalPos = playerData.getPortalPos() != null
                ? playerData.getPortalPos().above()
                : overworld.getSharedSpawnPos();

        Direction portalDir = playerData.getPortalDir();

        BlockPos exitPos = portalPos;
        if (portalDir != null) exitPos = portalPos.relative(portalDir, 8);


        float yaw = portalDir != null ? portalDir.toYRot() : player.getYRot();
        float pitch = 0.0F;

        DimensionTransition transition = new DimensionTransition(
                overworld,
                exitPos.getCenter(),
                Vec3.ZERO,
                yaw, pitch,
                DimensionTransition.DO_NOTHING
        );
        player.changeDimension(transition);

        instance.removePlayer(playerUuid);
        player.removeData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

        if (instance.isEmpty()) instance.remove();

        return true;
    }
}
