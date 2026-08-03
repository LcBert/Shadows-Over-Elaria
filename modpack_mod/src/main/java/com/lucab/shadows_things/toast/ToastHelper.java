package com.lucab.shadows_things.toast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class ToastHelper {
    public static void addToast(Player player, String text, String color, int duration) {
        if (player instanceof ServerPlayer serverPlayer) {
            addToast(serverPlayer, text, color, duration);
        }
    }

    public static void addToast(ServerPlayer player, String text, String color, int duration) {
        PacketDistributor.sendToPlayer(player, new ToastPacket(text, color, duration));
    }
}
