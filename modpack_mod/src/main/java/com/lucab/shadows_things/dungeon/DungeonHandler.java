package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DungeonHandler {
    @SubscribeEvent
    public static void onDungeonLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide || !level.dimension().equals(DungeonManager.DUNGEON_LEVEL_KEY)) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        ServerLevel dungeonLevel = server.getLevel(DungeonManager.DUNGEON_LEVEL_KEY);
        if (dungeonLevel == null) return;

        DungeonManager.getInternalMap().values().removeIf(instance -> instance.tickAndCheckExpiry(dungeonLevel));
    }

    @SubscribeEvent
    public static void asd(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide || !(event.getLevel() instanceof ServerLevel serverLevel)) return;

        if (event.getItemStack().is(Items.STICK)) {
            DungeonInstance dungeonInstance = DungeonManager.createDungeonInstance();
            dungeonInstance.addPlayer(event.getEntity());
            dungeonInstance.teleportPlayers(serverLevel, event.getEntity().getOnPos(), Direction.NORTH);
//            DungeonManager.removeDungeon(dungeonInstance.getId());
        } else if (event.getItemStack().is(Items.DIAMOND)) {
            event.getEntity().displayClientMessage(Component.literal(String.valueOf(DungeonManager.getDungeonInstances().size())), false);
        }
    }
}
