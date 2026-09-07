package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DungeonHandler {
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        DungeonManager.initializeServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        DungeonManager.clearServer();
    }

    @SubscribeEvent
    public static void onDungeonLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || !event.getLevel().dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY)) {
            return;
        }

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            List<DungeonInstance> instances = new ArrayList<>(DungeonManager.getDungeonInstances().values());
            for (DungeonInstance instance : instances) {
                instance.tick();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (DungeonManager.isPlayerInDungeon(event.getEntity()))
            DungeonManager.exitPlayer(event.getEntity().getUUID());
    }
}
