package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.dungeon.spawns.DungeonSpawnConfig;
import com.lucab.shadows_things.dungeon.spawns.DungeonSpawnEntry;
import com.lucab.shadows_things.toast.ToastHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DungeonRoom {
    public enum RoomType {
        NONE("none"),
        ENTRANCE("entrances"),
        ROOM("rooms"),
        STAIR("stairs"),
        EXIT("exits");

        private final String name;

        RoomType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static RoomType fromId(ResourceLocation templateLoc) {
            for (RoomType roomType : values()) {
                String templatePath = templateLoc.getPath();
                String type = templatePath.split("/")[2];

                if (roomType.name.equals(type)) {
                    return roomType;
                }
            }
            return NONE;
        }
    }

    public static final int ROOM_WIDTH = 33;
    public static final int ROOM_HEIGHT = 19;
    public static final int ROOM_LENGTH = 33;

    private final DungeonInstance instance;
    private final Vec3i gridPos;
    private final BlockPos originPos;
    private final AABB boundingBox;
    private final BlockPos.MutableBlockPos chestPos = new BlockPos.MutableBlockPos();

    private final ResourceLocation templateLocation;
    private final RoomType roomType;

    private final List<UUID> spawnedEntities = new ArrayList<>();
    private boolean chestPlaced = false;
    private boolean active = false;
    private boolean cleared = false;

    public DungeonRoom(DungeonInstance instance, Vec3i gridPos, BlockPos originPos, @Nullable ResourceLocation templateLocation) {
        this.instance = instance;
        this.gridPos = gridPos;
        this.originPos = originPos;
        this.templateLocation = templateLocation;
        this.roomType = RoomType.fromId(templateLocation);
        this.boundingBox = new AABB(
                originPos.getX(), originPos.getY(), originPos.getZ(),
                originPos.getX() + ROOM_WIDTH, originPos.getY() + ROOM_HEIGHT, originPos.getZ() + ROOM_LENGTH
        );
    }

    public ResourceLocation getTemplateLocation() {
        return templateLocation;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public boolean isRoom() {
        return this.getRoomType() == RoomType.ROOM;
    }

    public Vec3i getGridPos() {
        return gridPos;
    }

    public BlockPos getOriginPos() {
        return originPos;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCleared() {
        return cleared;
    }

    public boolean isChestPlaced() {
        return this.chestPlaced;
    }

    public boolean containsPlayer(ServerPlayer player) {
        return this.boundingBox.contains(player.position());
    }

    public void tick() {
        this.handlePlayerEnter();
        this.checkCleared();
    }

    private void handlePlayerEnter() {
        if (!this.isRoom() || this.isActive()) return;

        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        boolean anyInside = instance.getPlayers().stream()
                .map(server.getPlayerList()::getPlayer)
                .filter(Objects::nonNull)
                .anyMatch(this::containsPlayer);

        if (anyInside) {
            this.active = true;
            this.placeRoomChest();
            this.triggerSpawner();
        }
    }

    private void placeRoomChest() {
        ServerLevel level = DungeonManager.getDungeonLevel();
        if (level == null) return;

        BlockPos validPos = findValidSurfacePos();
        if (validPos != null) {
            this.chestPos.set(validPos);
            level.setBlock(this.chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            this.chestPlaced = true;
        }
    }

    private BlockPos findValidSurfacePos() {
        ServerLevel level = DungeonManager.getDungeonLevel();
        if (level == null) return null;

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();

        List<BlockPos> validPoses = new ArrayList<>();

        int minX = (int) this.getBoundingBox().minX;
        int minY = (int) this.getBoundingBox().minY;
        int minZ = (int) this.getBoundingBox().minZ;
        int maxX = (int) this.getBoundingBox().maxX;
        int maxY = (int) this.getBoundingBox().maxY;
        int maxZ = (int) this.getBoundingBox().maxZ;

        for (int attempts = 0; attempts < 64; attempts++) {
            int rx = rand.nextInt(minX + 1, maxX - 1);
            int rz = rand.nextInt(minZ + 1, maxZ - 1);

            for (int y = minY; y < maxY; y++) {
                probe.set(rx, y, rz);
                below.set(rx, y - 1, rz);
                above.set(rx, y + 1, rz);

                BlockState stateAtPos = level.getBlockState(probe);
                BlockState floorState = level.getBlockState(below);
                BlockState aboveState = level.getBlockState(above);

                boolean canPlaceChestHere = stateAtPos.isAir() || stateAtPos.canBeReplaced();
                boolean isSturdyFloor = floorState.isFaceSturdy(level, below, Direction.UP) && floorState.getFluidState().isEmpty();
                boolean isAboveNotObstructed = aboveState.isAir() || !aboveState.isSolidRender(level, above);

                if (canPlaceChestHere && isSturdyFloor && isAboveNotObstructed)
                    validPoses.add(probe.immutable());
            }
            if (!validPoses.isEmpty()) break;
        }

        if (validPoses.isEmpty()) return null;
        return validPoses.get(rand.nextInt(validPoses.size()));
    }

    public void triggerSpawner() {
        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        RandomSource randomSource = dungeonLevel.getRandom();

        if (this.isRoom()) {
            DungeonSpawnConfig spawnConfig = this.instance.getDungeonType().getSpawnConfig();
            if (spawnConfig == null) return;

            int totalSpawns = randomSource.nextIntBetweenInclusive(spawnConfig.getMinSpawns(), spawnConfig.getMaxSpawns());
            int spawnedCount = 0;
            while (spawnedCount < totalSpawns) {
                Optional<DungeonSpawnEntry> spawnEntryOpt = spawnConfig.getRandomEntry(randomSource);
                if (spawnEntryOpt.isEmpty()) continue;
                DungeonSpawnEntry spawnEntry = spawnEntryOpt.get();

                EntityType<?> entityType = spawnEntry.getEntityType();
                if (entityType == null) continue;

                Entity spawnedEntity = entityType.create(dungeonLevel);
                if (spawnedEntity == null) continue;

                BlockPos spawnPos = findValidSpawns(entityType);
                if (spawnPos == null) continue;

                spawnedEntity.moveTo(spawnPos.getCenter());

                if (spawnedEntity instanceof Mob mob) {
                    DifficultyInstance difficulty = dungeonLevel.getCurrentDifficultyAt(spawnPos);
                    EventHooks.finalizeMobSpawn(mob, dungeonLevel, difficulty, MobSpawnType.EVENT, null);
                    mob.setBaby(false);
                    mob.setPersistenceRequired();
                    this.spawnedEntities.add(mob.getUUID());
                }

                dungeonLevel.addFreshEntity(spawnedEntity);
                spawnedCount++;
            }
        }
    }

    private BlockPos findValidSpawns(EntityType<?> entityType) {
        List<BlockPos> validSpawns = new ArrayList<>();

        int minX = (int) this.getBoundingBox().minX;
        int minY = (int) this.getBoundingBox().minY;
        int minZ = (int) this.getBoundingBox().minZ;
        int maxX = (int) this.getBoundingBox().maxX;
        int maxY = (int) this.getBoundingBox().maxY;
        int maxZ = (int) this.getBoundingBox().maxZ;

        int randomX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int randomZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = minY; y < maxY; y++) {
            pos.set(randomX, y, randomZ);
            if (this.isValidSpawn(entityType, pos)) {
                validSpawns.add(pos.immutable());
            }
        }

        if (validSpawns.isEmpty()) return null;
        return validSpawns.get(ThreadLocalRandom.current().nextInt(validSpawns.size()));
    }

    private boolean isValidSpawn(EntityType<?> entityType, BlockPos pos) {
        ServerLevel level = DungeonManager.getDungeonLevel();
        if (level == null) return false;

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) return false;

        if (!belowState.getFluidState().isEmpty() || !level.getFluidState(pos).isEmpty()) return false;

        float entityHeight = entityType.getDimensions().height();
        int heightBlocks = Math.min(1, (int) Math.ceil(entityHeight));

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int h = 0; h < heightBlocks; h++) {
            checkPos.set(pos.getX(), pos.getY(), pos.getZ());
            BlockState state = level.getBlockState(checkPos);

            if (state.isSuffocating(level, checkPos)) return false;
        }

        AABB mobBox = entityType.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return level.noCollision(mobBox);
    }

    private void checkCleared() {
        if (!this.isActive() || this.isCleared()) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        this.spawnedEntities.removeIf(uuid -> {
            Entity entity = dungeonLevel.getEntity(uuid);
            return entity == null || !entity.isAlive() || entity.isRemoved();
        });

        if (this.spawnedEntities.isEmpty()) this.onRoomCleared();
    }

    private void onRoomCleared() {
        this.cleared = true;

        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        for (UUID uuid : this.instance.getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                ToastHelper.addToast(player, "Room Cleared", ChatFormatting.GREEN, 100, SoundEvents.PLAYER_LEVELUP);
            }
        }
    }

    public void cleanup() {
        ServerLevel level = DungeonManager.getDungeonLevel();
        if (level == null) return;

        for (UUID entityUUID : this.spawnedEntities) {
            Entity entity = level.getEntity(entityUUID);
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
        }
        this.spawnedEntities.clear();

        if (isChestPlaced()) {
            level.setBlock(this.chestPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            chestPlaced = false;
        }
    }
}
