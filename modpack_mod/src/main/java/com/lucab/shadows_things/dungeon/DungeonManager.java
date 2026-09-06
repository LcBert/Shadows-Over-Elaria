package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.dungeon_portal_block.DungeonPortalEntity;
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
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DungeonManager {
    protected static final int DUNGEON_SIZE = 250;
    private static final int DUNGEON_OFFSET = 512;

    public static final ResourceKey<Level> DUNGEON_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "dungeon")
    );

    private static final Map<Long, DungeonInstance> DUNGEON_INSTANCES = new HashMap<>();

    public static ServerLevel getDungeonLevel(Level level) {
        if (level == null || level.isClientSide()) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        return server.getLevel(DUNGEON_LEVEL_KEY);
    }

    public static DungeonInstance createDungeonInstance() {
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
            if (instance.getPlayers().contains(player)) {
                return instance;
            }
        }
        return null;
    }

    public static void exitPlayers(List<Player> players) {
        for (Player player : players) {
            exitPlayer(player);
        }
    }

    public static void exitPlayer(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        DungeonPlayerData playerData = player.getData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

        BlockPos portalPos = playerData.getPortalPos() != null
                ? playerData.getPortalPos().above()
                : overworld.getSharedSpawnPos();

        Direction portalDir = playerData.getPortalDir();

        BlockPos exitPos = portalPos.relative(portalDir, 8);

        float xRot = portalDir.toYRot();
        float yRot = 0.0F;

        DimensionTransition transition = new DimensionTransition(
                overworld,
                exitPos.getCenter(),
                Vec3.ZERO,
                xRot, yRot,
                DimensionTransition.DO_NOTHING
        );
        serverPlayer.changeDimension(transition);
    }
}
