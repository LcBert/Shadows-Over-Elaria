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
        CRYPT("crypt", new DungeonSpawnConfig(4, 8, List.of(
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
    private static final Map<Long, DungeonInstance> ACTIVE_INSTANCES = new HashMap<>();
    private static final Map<UUID, DungeonInstance> PLAYER_INSTANCE_CACHE = new HashMap<>();

    public static void initializeServer(MinecraftServer server) {
        DungeonManager.server = server;
    }

    public static void clearServer() {
        ACTIVE_INSTANCES.clear();
        PLAYER_INSTANCE_CACHE.clear();
        server = null;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static ServerLevel getDungeonLevel() {
        return server != null ? server.getLevel(DUNGEON_LEVEL_KEY) : null;
    }

    public static DungeonInstance createDungeonInstance() {
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return null;

        BlockPos spawnPos;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long id;
        int attempts = 0;

        do {
            int xOffset = random.nextInt(2049) - 1024;
            int zOffset = random.nextInt(2049) - 1024;
            spawnPos = new BlockPos(DUNGEON_OFFSET * xOffset, 200, DUNGEON_OFFSET * zOffset);
            id = spawnPos.asLong();
            attempts++;
        } while (ACTIVE_INSTANCES.containsKey(id) && attempts < 100);

        DungeonInstance instance = new DungeonInstance(id, spawnPos);
        ACTIVE_INSTANCES.put(instance.getId(), instance);
        return instance;
    }

    public static void registerPlayerInDungeon(UUID uuid, DungeonInstance instance) {
        PLAYER_INSTANCE_CACHE.put(uuid, instance);
    }

    public static void unregisterPlayerFromDungeon(UUID uuid) {
        PLAYER_INSTANCE_CACHE.remove(uuid);
    }

    public static void removeDungeon(long id) {
        DungeonInstance instance = ACTIVE_INSTANCES.remove(id);
        if (instance != null) {
            instance.getPlayers().forEach(PLAYER_INSTANCE_CACHE::remove);
        }
    }

    public static Collection<DungeonInstance> getDungeonInstances() {
        return Collections.unmodifiableCollection(ACTIVE_INSTANCES.values());
    }

    public static DungeonInstance getInstanceForPlayer(Player player) {
        return getInstanceForPlayer(player.getUUID());
    }

    public static DungeonInstance getInstanceForPlayer(UUID uuid) {
        return PLAYER_INSTANCE_CACHE.get(uuid);
    }

    public static boolean addPlayerToDungeon(UUID uuid, DungeonInstance targetIntance) {
        if (uuid == null || targetIntance == null) return false;

        DungeonInstance currentInstance = getInstanceForPlayer(uuid);
        if (currentInstance != null) {
            if (currentInstance.getId() == targetIntance.getId()) return true;

            currentInstance.removePlayer(uuid);

            if (currentInstance.isEmpty()) currentInstance.remove();
        }

        targetIntance.addPlayer(uuid);
        registerPlayerInDungeon(uuid, targetIntance);

        return true;
    }

    public static void addPlayersToDungeon(Collection<UUID> playerUuids, DungeonInstance targetInstance) {
        if (playerUuids == null || targetInstance == null) return;
        for (UUID uuid : playerUuids) {
            addPlayerToDungeon(uuid, targetInstance);
        }
    }

    public static List<Boolean> exitPlayers(Collection<UUID> players) {
        List<Boolean> results = new ArrayList<>(players.size());
        for (UUID player : players) {
            results.add(exitPlayer(player));
        }
        return results;
    }

    public static boolean exitPlayer(UUID playerUuid) {
        if (server == null) return false;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        if (overworld == null || player == null) return false;

        DungeonInstance instance = getInstanceForPlayer(player);
        if (instance == null) return false;

        DungeonPlayerData playerData = player.getData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

        BlockPos exitPos = Optional.ofNullable(playerData.getPortalPos())
                .map(BlockPos::above)
                .orElse(overworld.getSharedSpawnPos());

        Direction portalDir = playerData.getPortalDir();
        if (portalDir != null) {
            exitPos = exitPos.relative(portalDir, 8);
        }

        float yaw = portalDir != null ? portalDir.toYRot() : player.getYRot();

        DimensionTransition transition = new DimensionTransition(
                overworld,
                exitPos.getCenter(),
                Vec3.ZERO,
                yaw, 0.0F,
                DimensionTransition.DO_NOTHING
        );
        player.changeDimension(transition);

        instance.removePlayer(playerUuid);
        player.removeData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

        if (instance.isEmpty()) instance.remove();

        return true;
    }
}
