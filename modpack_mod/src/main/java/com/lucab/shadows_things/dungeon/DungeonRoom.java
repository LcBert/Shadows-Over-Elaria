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
        STAIR("stairs");

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

    private final ResourceLocation templateLocation;
    private final RoomType roomType;

    private final List<UUID> spawnedEntities = new ArrayList<>();
    private boolean cleared = false;
    private boolean active = false;

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

    public Vec3i getGridPos() {
        return gridPos;
    }

    public BlockPos getOriginPos() {
        return originPos;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public boolean containsPlayer(ServerPlayer player) {
        return this.boundingBox.contains(player.position());
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void tick() {
        this.handleSpawner();
    }

    public void handleSpawner() {
        if (this.isCleared()) return;
        this.checkCleared();

        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        RandomSource randomSource = dungeonLevel.getRandom();

        if (this.getRoomType() == RoomType.ROOM && !this.isActive()) {
            boolean containsPlayer = instance.getPlayers().stream()
                    .map(server.getPlayerList()::getPlayer)
                    .filter(Objects::nonNull)
                    .anyMatch(this::containsPlayer);

            if (containsPlayer) {
                this.setActive(true);

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
                    if (spawnedEntity == null) return;
                    List<BlockPos> validSpawns = findValidSpawns(entityType);
                    if (validSpawns.isEmpty()) return;

                    BlockPos spawnPos = validSpawns.get(ThreadLocalRandom.current().nextInt(validSpawns.size()));

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
        this.setCleared(true);

        MinecraftServer server = DungeonManager.getServer();
        if (server == null) return;

        this.instance.getPlayers().stream()
                .map(server.getPlayerList()::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    ToastHelper.addToast(player,
                            "Room Cleared",
                            ChatFormatting.GREEN,
                            100,
                            SoundEvents.PLAYER_LEVELUP);
                });
    }

    private List<BlockPos> findValidSpawns(EntityType<?> entityType) {
        List<BlockPos> validSpawns = new ArrayList<>();

        int minX = (int) this.getBoundingBox().minX;
        int minY = (int) this.getBoundingBox().minY;
        int minZ = (int) this.getBoundingBox().minZ;
        int maxX = (int) this.getBoundingBox().maxX;
        int maxY = (int) this.getBoundingBox().maxY;
        int maxZ = (int) this.getBoundingBox().maxZ;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    pos.set(x, y, z);
                    if (this.isValidSpawn(entityType, pos)) {
                        validSpawns.add(pos.immutable());
                    }
                }
            }
        }

        return Collections.unmodifiableList(validSpawns);
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

    public void cleanupEntities() {
        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null) return;

        for (UUID entityUUID : this.spawnedEntities) {
            Entity entity = dungeonLevel.getEntity(entityUUID);
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
        }
        this.spawnedEntities.clear();
    }
}
