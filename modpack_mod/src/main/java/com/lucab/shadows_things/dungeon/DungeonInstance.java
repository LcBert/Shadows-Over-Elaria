package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.dungeon_portal_block.DungeonPortalEntity;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Random;

import java.util.*;

public class DungeonInstance {
    private static final int EMPTY_TIMEOUT_TICKS = DungeonPortalEntity.ENTRANCE_TICK + 100;

    private final long id;
    private final BlockPos dungeonCenter;
    private final AABB boundingBox;
    private final List<Player> players = new ArrayList<>();
    private int emptyTicks = 0;

    public DungeonInstance(long id, BlockPos dungeonCenter) {
        this.id = id;
        this.dungeonCenter = dungeonCenter;

        int halfSize = DungeonManager.DUNGEON_SIZE / 2;
        this.boundingBox = new AABB(
                dungeonCenter.getX() - halfSize, dungeonCenter.getY(), dungeonCenter.getZ() - halfSize,
                dungeonCenter.getX() + halfSize, dungeonCenter.getY() + DungeonManager.DUNGEON_SIZE, dungeonCenter.getZ() + halfSize
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

    public void addPlayer(Player player) {
        if (!this.players.contains(player)) this.players.add(player);
    }

    public void addPlayers(List<Player> players) {
        for (Player player : players) this.addPlayer(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean tickAndCheckExpiry(ServerLevel level) {
        boolean hasPlayersInside = !level.getEntitiesOfClass(Player.class, boundingBox).isEmpty();

        if (hasPlayersInside) {
            this.emptyTicks = 0;
            return false;
        }

        this.emptyTicks++;
        return this.emptyTicks >= EMPTY_TIMEOUT_TICKS;
    }

    public void teleportPlayers(Level level, BlockPos portalPos, Direction portalDir) {
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel(level);
        if (dungeonLevel == null) return;

        for (Player player : players) {
            DungeonPlayerData playerData = player.getData(DungeonPlayerData.DUNGEON_PLAYER_DATA);
            playerData.setPortalPos(portalPos);
            playerData.setPortalDir(portalDir);

            if (player instanceof ServerPlayer serverPlayer) {
                DimensionTransition transition = new DimensionTransition(
                        dungeonLevel,
                        getDungeonCenter().getCenter(),
                        Vec3.ZERO,
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot(),
                        DimensionTransition.DO_NOTHING
                );
                serverPlayer.changeDimension(transition);
            }
        }
    }
}
