package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.content.block.dungeon_portal_block.DungeonPortalEntity;
import com.lucab.shadows_things.rpg.classes.ClassPlayerData;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DungeonInstance {
    private static final TicketType<ChunkPos> DUNGEON_LOAD_TICKET = TicketType.create(
            "dungeon_loader",
            Comparator.comparingLong(ChunkPos::toLong),
            600
    );

    private static final int OCCUPANCY_CHECK_INTERVAL = 20;
    private static final int EMPTY_TIMEOUT_TICKS = DungeonPortalEntity.ENTRANCE_TICK + 100;

    private final long id;
    private final BlockPos dungeonCenter;
    private final AABB boundingBox;
    private final Set<UUID> players = new HashSet<>();
    private final Map<Vec3i, DungeonRoom> roomGrid = new HashMap<>();
    private final List<DungeonRoom> rooms = new ArrayList<>();

    private DungeonManager.DungeonType dungeonType;
    private ChunkPos loadedTicket = null;
    private CompletableFuture<Void> preparationFuture = null;

    private boolean generatedAndReady = false;
    private boolean started = false;
    private int emptyTicks = 0;
    private int checkCooldown = 0;
    private boolean currentlyOccupied = true;

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

    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public List<DungeonRoom> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public DungeonManager.DungeonType getDungeonType() {
        return dungeonType;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isGeneratedAndReady() {
        return generatedAndReady;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }


    public void addPlayer(UUID uuid) {
        this.players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
        DungeonManager.unregisterPlayerFromDungeon(uuid);
    }

    public int getAverageTier() {
        if (this.isEmpty() || DungeonManager.getServer() == null) return -1;

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

    @Nullable
    public DungeonRoom getRoomAtWorld(BlockPos pos) {
        if (!boundingBox.contains(pos.getX(), pos.getY(), pos.getZ())) return null;
        DungeonRoom room = roomGrid.get(worldToGridPos(pos));
        return (room != null && room.getBoundingBox().contains(pos.getX(), pos.getY(), pos.getZ())) ? room : null;
    }

    public void tick() {
        this.checkExpireAndCleanup();

        if (this.isGeneratedAndReady()) {
            for (DungeonRoom room : this.rooms) {
                room.tick();
            }
        }
    }

    public CompletableFuture<Void> prepareAndTeleportPlayers(@Nullable BlockPos portalPos, @Nullable Direction portalDir) {
        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return CompletableFuture.completedFuture(null);

        return this.prepareStructure().thenRunAsync(() -> {
            this.teleportPlayers(portalPos, portalDir);
        }, server);
    }

    public CompletableFuture<Void> prepareStructure() {
        if (isGeneratedAndReady()) return CompletableFuture.completedFuture(null);
        if (this.preparationFuture != null) return this.preparationFuture;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Dungeon level is not available"));
        }

        MinecraftServer server = DungeonManager.getServer();
        if (server == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Server instance is not available"));
        }

        ServerChunkCache chunkSource = dungeonLevel.getChunkSource();
        ChunkPos centerChunk = new ChunkPos(dungeonCenter);

        chunkSource.addRegionTicket(DUNGEON_LOAD_TICKET, centerChunk, 2, centerChunk);
        this.loadedTicket = centerChunk;

        this.preparationFuture = chunkSource.getChunkFuture(centerChunk.x, centerChunk.z, ChunkStatus.FULL, true)
                .thenAcceptAsync(fullResult -> {
                    fullResult.ifSuccess(chunk -> {
                        this.scanAndPopulateRooms(dungeonLevel, chunk);
                        this.generatedAndReady = true;
                    });
                });

        return this.preparationFuture;
    }

    public void scanAndPopulateRooms(ServerLevel level, ChunkAccess centerChunkAccess) {
        StructureStart start = null;
        SectionPos sectionPos = SectionPos.of(this.dungeonCenter);

        for (DungeonManager.DungeonType type : DungeonManager.DungeonType.values()) {
            Structure structure = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .get(type.getKey());

            if (structure == null) continue;

            start = level.structureManager().getStartForStructure(
                    sectionPos,
                    structure,
                    centerChunkAccess
            );

            if (start != null && start.isValid()) {
                this.dungeonType = type;
                break;
            }
        }

        if (start == null || !start.isValid()) return;

        this.roomGrid.clear();
        this.rooms.clear();

        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof PoolElementStructurePiece poolPiece) {
                BoundingBox bb = poolPiece.getBoundingBox();

                BlockPos pieceCenter = new BlockPos(
                        bb.minX() + (bb.getXSpan() / 2),
                        bb.minY() + (bb.getYSpan() / 2),
                        bb.minZ() + (bb.getZSpan() / 2)
                );

                Vec3i gridPos = worldToGridPos(pieceCenter);
                BlockPos originPos = gridToWorldPos(gridPos);
                ResourceLocation templateLocation = DungeonStructureScanner.resolveElementTemplate(poolPiece.getElement());

                DungeonRoom room = new DungeonRoom(this, gridPos, originPos, templateLocation);

                this.roomGrid.put(gridPos, room);
                this.rooms.add(room);
            }
        }
    }

    public BlockPos gridToWorldPos(Vec3i gridPos) {
        int originX = this.dungeonCenter.getX() + (gridPos.getX() * DungeonRoom.ROOM_WIDTH) - (DungeonRoom.ROOM_WIDTH / 2);
        int originY = this.dungeonCenter.getY() + (gridPos.getY() * DungeonRoom.ROOM_HEIGHT);
        int originZ = this.dungeonCenter.getZ() + (gridPos.getZ() * DungeonRoom.ROOM_LENGTH) - (DungeonRoom.ROOM_LENGTH / 2);
        return new BlockPos(originX, originY, originZ);
    }

    /**
     * Converts a world BlockPos into its logical grid coordinate.
     */
    public Vec3i worldToGridPos(BlockPos pos) {
        int adjustedX = pos.getX() - (this.dungeonCenter.getX() - (DungeonRoom.ROOM_WIDTH / 2));
        int adjustedY = pos.getY() - this.dungeonCenter.getY();
        int adjustedZ = pos.getZ() - (this.dungeonCenter.getZ() - (DungeonRoom.ROOM_LENGTH / 2));

        return new Vec3i(
                Math.floorDiv(adjustedX, DungeonRoom.ROOM_WIDTH),
                Math.floorDiv(adjustedY, DungeonRoom.ROOM_HEIGHT),
                Math.floorDiv(adjustedZ, DungeonRoom.ROOM_LENGTH)
        );
    }

    private void checkExpireAndCleanup() {
        if (!isStarted()) return;

        if (--this.checkCooldown <= 0) {
            this.checkCooldown = OCCUPANCY_CHECK_INTERVAL;
            this.currentlyOccupied = checkPlayerPresence();
        }

        if (this.currentlyOccupied) {
            this.emptyTicks = 0;
        } else if (++this.emptyTicks >= EMPTY_TIMEOUT_TICKS) {
            this.remove();
        }
    }

    private boolean checkPlayerPresence() {
        MinecraftServer server = DungeonManager.getServer();
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (server == null || dungeonLevel == null) return false;

        PlayerList playerList = server.getPlayerList();

        for (UUID uuid : this.players) {
            ServerPlayer player = playerList.getPlayer(uuid);
            if (player != null && !player.isRemoved() && player.isAlive() && player.level() == dungeonLevel) {
                if (this.getBoundingBox().contains(player.position())) {
                    return true;
                }
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

        this.started = true;
        this.emptyTicks = 0;
        this.currentlyOccupied = true;
    }

    public void remove() {
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel != null) {
            for (DungeonRoom room : this.rooms) {
                room.cleanup();
            }

            if (this.loadedTicket != null) {
                ServerChunkCache chunkSource = dungeonLevel.getChunkSource();
                chunkSource.removeRegionTicket(DUNGEON_LOAD_TICKET, this.loadedTicket, 2, this.loadedTicket);
                this.loadedTicket = null;
            }
        }

        this.rooms.clear();
        this.roomGrid.clear();
        this.players.clear();
        DungeonManager.removeDungeon(this.getId());
    }
}
