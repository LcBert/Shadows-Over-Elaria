package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DungeonHandler {
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        DungeonManager.initializeServer(event.getServer());
        DungeonStructureScanner.HIGHLIGHTED_PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        DungeonManager.clearServer();
        DungeonStructureScanner.HIGHLIGHTED_PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onDungeonLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || !event.getLevel().dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY)) {
            return;
        }

        if (event.getLevel() instanceof ServerLevel) {
            List<DungeonInstance> instances = new ArrayList<>(DungeonManager.getDungeonInstances().values());
            for (DungeonInstance instance : instances) {
                instance.tick();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDropInDungeon(LivingDropsEvent event) {
        if (event.getEntity().level().dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);
            if (instance != null) {
                instance.removePlayer(player.getUUID());
                player.removeData(DungeonPlayerData.DUNGEON_PLAYER_DATA);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getFrom().equals(DungeonManager.DUNGEON_LEVEL_KEY) && event.getEntity() instanceof ServerPlayer player) {
            DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);
            if (instance != null) {
                instance.removePlayer(player.getUUID());
                player.removeData(DungeonPlayerData.DUNGEON_PLAYER_DATA);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);
            if (instance != null) {
                instance.removePlayer(player.getUUID());
            }
        }
    }
}
