package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.content.block.dungeon_portal_block.DungeonPortalEntity;
import com.lucab.shadows_things.rpg.classes.ClassPlayerData;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DungeonInstance {
    private static final int CHECK_INTERVAL_TICKS = 20; // Check once every second
    private static final int EMPTY_TIMEOUT_TICKS = DungeonPortalEntity.ENTRANCE_TICK + 100;

    private final long id;
    private final BlockPos dungeonCenter;
    private final AABB boundingBox;
    private final Set<UUID> players = new HashSet<>();

    private boolean isStarted = false;
    private int emptyTicks = 0;
    private int checkCooldown = 0;
    private boolean isCurrentlyOccupied = true;

    public DungeonInstance(long id, BlockPos dungeonCenter) {
        this.id = id;
        this.dungeonCenter = dungeonCenter;

        int dungeonSize = DungeonManager.DUNGEON_SIZE;
        this.boundingBox = new AABB(
                dungeonCenter.getX() - dungeonSize, dungeonCenter.getY() - dungeonSize, dungeonCenter.getZ() - dungeonSize,
                dungeonCenter.getX() + dungeonSize, dungeonCenter.getY() + dungeonSize, dungeonCenter.getZ() + dungeonSize
        );
    }

    public long getId() {
        return id;
    }

    public BlockPos getDungeonCenter() {
        return this.dungeonCenter;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public void addPlayer(UUID player) {
        if (!this.players.contains(player)) this.players.add(player);
    }

    public void addPlayers(List<UUID> players) {
        for (UUID player : players) this.addPlayer(player);
    }

    public void removePlayer(UUID player) {
        this.players.remove(player);
    }

    public void removePlayers(List<UUID> players) {
        for (UUID player : players) this.removePlayer(player);
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public int getPlayersCount() {
        return this.players.size();
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public int getAverageTier() {
        if (this.getPlayersCount() == 0) return -1;

        PlayerList playerList = DungeonManager.getServer().getPlayerList();

        return (int) Math.round(
                this.players.stream()
                        .map(playerList::getPlayer)
                        .filter(Objects::nonNull)
                        .map(ClassPlayerData::getClassData)
                        .mapToInt(ClassPlayerData::getClassTier)
                        .average()
                        .orElse(-1.0)
        );
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void tick() {
        this.checkExpireAndCleanup();
    }

    private void checkExpireAndCleanup() {
        if (!isStarted()) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        if (--this.checkCooldown <= 0) {
            this.checkCooldown = CHECK_INTERVAL_TICKS;
            this.isCurrentlyOccupied = evaluatePlayerPresence(dungeonLevel);
        }

        if (this.isCurrentlyOccupied) {
            this.emptyTicks = 0;
        } else {
            this.emptyTicks++;
            if (this.emptyTicks >= EMPTY_TIMEOUT_TICKS) {
                this.remove();
            }
        }
    }

    private boolean evaluatePlayerPresence(ServerLevel dungeonLevel) {
        MinecraftServer server = dungeonLevel.getServer();
        PlayerList playerList = server.getPlayerList();

        for (UUID uuid : this.players) {
            ServerPlayer player = playerList.getPlayer(uuid);

            // Skip if offline or dead/removed
            if (player == null || player.isRemoved() || !player.isAlive()) {
                continue;
            }

            // Verify player is in this dungeon dimension and within the instance bounding box
            if (player.level() == dungeonLevel && this.boundingBox.contains(player.position())) {
                return true;
            }
        }

        return false;
    }

    public void teleportPlayers(@Nullable BlockPos portalPos, @Nullable Direction portalDir) {
        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        for (UUID playerUuid : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
            if (player == null) continue;

            DungeonPlayerData playerData = player.getData(DungeonPlayerData.DUNGEON_PLAYER_DATA);

            playerData.setPortalPos(portalPos != null ? portalPos : player.getOnPos());
            playerData.setPortalDir(portalDir);

            DimensionTransition transition = new DimensionTransition(
                    dungeonLevel,
                    getDungeonCenter().getCenter(),
                    Vec3.ZERO,
                    player.getYRot(),
                    player.getXRot(),
                    DimensionTransition.DO_NOTHING
            );
            player.changeDimension(transition);
        }

        setStarted(true);
        this.emptyTicks = 0;
        this.isCurrentlyOccupied = true;
    }

    public void remove() {
        DungeonManager.removeDungeon(this.getId());
    }
}
