package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DungeonHandler {
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        DungeonManager.initializeServer(event.getServer());
        DungeonStructureScanner.clearHighlights();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        DungeonManager.clearServer();
        DungeonStructureScanner.clearHighlights();
    }

    @SubscribeEvent
    public static void onDungeonLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || !event.getLevel().dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY)) {
            return;
        }

        if (event.getLevel() instanceof ServerLevel) {
            for (DungeonInstance instance : DungeonManager.getDungeonInstances()) {
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
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DungeonManager.exitPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onChestOpened(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);
        if (instance == null) return;

        ServerLevel dungeonLevel = DungeonManager.getDungeonLevel();
        if (dungeonLevel == null || !dungeonLevel.getBlockState(event.getPos()).is(Blocks.CHEST)) return;

        DungeonRoom room = instance.getRoomAtWorld(event.getPos());
        if (room != null && !room.isCleared()) {
            player.displayClientMessage(Component.literal("This room is not cleared yet."), true);
            player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            event.setCanceled(true);
        }
    }
}
