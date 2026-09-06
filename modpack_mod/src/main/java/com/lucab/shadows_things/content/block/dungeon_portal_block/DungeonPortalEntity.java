package com.lucab.shadows_things.content.block.dungeon_portal_block;

import com.lucab.shadows_things.Utils;
import com.lucab.shadows_things.dungeon.DungeonInstance;
import com.lucab.shadows_things.dungeon.DungeonManager;
import com.lucab.shadows_things.dungeon.DungeonPlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.*;

public class DungeonPortalEntity extends BlockEntity {
    protected static final int ENTRANCE_RADIUS = 3;
    public static final int ENTRANCE_TICK = 300;
    private static final int ENTRANCE_EFFECTS_TICK = 50;
    private static final int ENTRANCE_FIRST_MESSAGE_TICK = 50;
    private static final int ENTRANCE_SECOND_MESSAGE_TICK = 150;

    private DungeonInstance dungeonInstance = null;
    private int tickCount = 0;

    private final Map<UUID, Long> playerExitTimes = new HashMap<>();

    public DungeonPortalEntity(BlockPos pos, BlockState state) {
        super(DungeonPortalRegister.DUNGEON_PORTAL_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DungeonPortalEntity portal) {
        portal.tickCount++;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.0;
        double centerZ = pos.getZ() + 0.5;

        // 1. Pulsing Crimson Boundary & Glyph Orbit
        if (portal.tickCount % 4 == 0) {
            // Slight breathing pulse on radius: from 2.85 to 3.15
            double pulseRadius = ENTRANCE_RADIUS + Math.sin(portal.tickCount * 0.08) * 0.15;
            DustParticleOptions crimsonDust = new DustParticleOptions(new Vector3f(0.85f, 0.05f, 0.1f), 1.0f);

            int borderPoints = 24;
            for (int i = 0; i < borderPoints; i++) {
                double angle = (2 * Math.PI / borderPoints) * i;
                double x = centerX + Math.cos(angle) * pulseRadius;
                double z = centerZ + Math.sin(angle) * pulseRadius;

                level.addParticle(crimsonDust, x, centerY + 0.1, z, 0.0, 0.02, 0.0);
            }
        }

        // Floating orbiting glyphs along the boundary
        if (portal.tickCount % 2 == 0) {
            double glyphAngle = portal.tickCount * 0.05;
            double glyphX = centerX + Math.cos(glyphAngle) * ENTRANCE_RADIUS;
            double glyphZ = centerZ + Math.sin(glyphAngle) * ENTRANCE_RADIUS;
            level.addParticle(ParticleTypes.ENCHANT, glyphX, centerY + 0.2, glyphZ, 0.0, 0.1, 0.0);
        }

        // 2. Rising Double Helix (Two intertwined spiraling strands)
        int strands = 2;
        int helixSteps = 3;
        double maxHeight = 3.2;

        for (int s = 0; s < strands; s++) {
            double strandOffset = s * Math.PI; // 180 deg phase shift

            for (int step = 0; step < helixSteps; step++) {
                // Progress through a vertical cycle (0.0 to 1.0)
                double cycleProgress = ((portal.tickCount * 2 + step * 7) % 60) / 60.0;
                double currentHeight = centerY + (cycleProgress * maxHeight);

                // Radius tapers inward as it rises
                double currentRadius = (ENTRANCE_RADIUS * 0.85) * (1.0 - (cycleProgress * 0.5));
                double angle = (portal.tickCount * 0.12) + strandOffset + (cycleProgress * Math.PI * 4.0);

                double x = centerX + Math.cos(angle) * currentRadius;
                double z = centerZ + Math.sin(angle) * currentRadius;

                level.addParticle(ParticleTypes.WITCH, x, currentHeight, z, 0.0, 0.01, 0.0);
            }
        }

        // 3. Central Dimensional Core (Imploding / condensing vortex)
        for (int i = 0; i < 2; i++) {
            double offsetAngle = level.random.nextDouble() * Math.PI * 2;
            double offsetDist = level.random.nextDouble() * 0.8;
            double coreX = centerX + Math.cos(offsetAngle) * offsetDist;
            double coreZ = centerZ + Math.sin(offsetAngle) * offsetDist;
            double coreY = centerY + level.random.nextDouble() * 2.0;

            level.addParticle(ParticleTypes.REVERSE_PORTAL, coreX, coreY, coreZ, 0.0, 0.0, 0.0);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DungeonPortalEntity portal) {
        if (level == null) return;

        List<Player> nearbyPlayers = portal.getNearbyPlayers();

        if (nearbyPlayers.isEmpty()) {
            portal.tickCount = 0;
            portal.dungeonInstance = null;
            portal.playerExitTimes.clear();
            return;
        }

        portal.tickCount++;

        if (portal.tickCount == 1) {
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (portal.tickCount > 0 && portal.tickCount % 60 == 0) {
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        // Add Effects
        if (portal.tickCount >= ENTRANCE_EFFECTS_TICK) {
            nearbyPlayers.forEach(player -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1, false, false));
            });
        }

        // Show messages
        if (portal.tickCount == ENTRANCE_FIRST_MESSAGE_TICK) {
            nearbyPlayers.forEach(player -> {
                Utils.sendTitleMessage(player,
                        Component.literal("Allineamento al portale"),
                        Component.literal("La tua essenza si sta allineando al portale")
                );
            });
        }

        if (portal.tickCount == ENTRANCE_SECOND_MESSAGE_TICK) {
            nearbyPlayers.forEach(player -> {
                if (!portal.isInDungeon()) {
                    Utils.sendTitleMessage(player,
                            Component.literal("Portale aperto"),
                            Component.literal("Preparati ad entrare")
                    );
                } else {
                    Utils.sendTitleMessage(player,
                            Component.literal("Portale aperto"),
                            Component.literal("Preparati ad uscire")
                    );

                }
            });
        }

        if (!portal.isInDungeon()) {
            if (portal.dungeonInstance == null) {
                portal.dungeonInstance = DungeonManager.createDungeonInstance();
                portal.dungeonInstance.addPlayers(nearbyPlayers);
            }

            if (portal.tickCount >= ENTRANCE_TICK) {
                portal.dungeonInstance.teleportPlayers(
                        level,
                        portal.getBlockPos(),
                        level.getBlockState(portal.getBlockPos()).getValue(DungeonPortalBlock.FACING)
                );
                portal.dungeonInstance = null;
            }
        } else {
            long currentGameTime = level.getGameTime();

            Set<UUID> nearbyPlayerUUIDs = new HashSet<>();

            for (Player player : nearbyPlayers) {
                UUID uuid = player.getUUID();
                nearbyPlayerUUIDs.add(uuid);

                portal.playerExitTimes.putIfAbsent(uuid, currentGameTime);

                long startTick = portal.playerExitTimes.get(uuid);
                if (currentGameTime - startTick >= ENTRANCE_TICK) {
                    DungeonManager.exitPlayer(player);
                    portal.playerExitTimes.remove(uuid);
                }
            }

            portal.playerExitTimes.keySet().removeIf(uuid -> !nearbyPlayerUUIDs.contains(uuid));
        }
    }

    private List<Player> getNearbyPlayers() {
        if (level == null) return List.of();
        BlockPos pos = this.getBlockPos();

        AABB searchBox = new AABB(
                pos.getX() - ENTRANCE_RADIUS, pos.getY() + 1, pos.getZ() - ENTRANCE_RADIUS,
                pos.getX() + 1 + ENTRANCE_RADIUS, pos.getY() + 4, pos.getZ() + 1 + ENTRANCE_RADIUS
        );

        double radiusSq = ENTRANCE_RADIUS * ENTRANCE_RADIUS;
        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        return level.getEntities(
                EntityTypeTest.forClass(Player.class),
                searchBox,
                player -> {
                    double playerY = player.getY();
                    if (playerY < pos.getY() + 1 || playerY > pos.getY() + 4) {
                        return false;
                    }
                    double dx = player.getX() - centerX;
                    double dz = player.getZ() - centerZ;
                    return (dx * dx + dz * dz) <= radiusSq;
                }
        );
    }

    private boolean isInDungeon() {
        if (level == null) return false;
        return level.dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY);
    }
}
