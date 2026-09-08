package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.level.ServerLevel;
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
    private static final Set<UUID> HIGHLIGHTED_PLAYERS = new HashSet<>();

    public static boolean toggleHighlight(UUID playerUuid) {
        if (HIGHLIGHTED_PLAYERS.contains(playerUuid)) {
            HIGHLIGHTED_PLAYERS.remove(playerUuid);
            return false;
        } else {
            HIGHLIGHTED_PLAYERS.add(playerUuid);
            return true;
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        DungeonManager.initializeServer(event.getServer());
        HIGHLIGHTED_PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        DungeonManager.clearServer();
        HIGHLIGHTED_PLAYERS.clear();
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
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (DungeonManager.isPlayerInDungeon(event.getEntity()))
            DungeonManager.exitPlayer(event.getEntity().getUUID());
    }
}
