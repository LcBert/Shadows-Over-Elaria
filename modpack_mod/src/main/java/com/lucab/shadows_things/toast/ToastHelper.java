package com.lucab.shadows_things.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Optional;

public class ToastHelper {
    public static void addToast(Player player, String text, ChatFormatting color, int duration, @Nullable SoundEvent sound) {
        ResourceLocation soundLoc = sound != null ? BuiltInRegistries.SOUND_EVENT.getKey(sound) : null;
        if (player instanceof ServerPlayer serverPlayer) {
            addToast(serverPlayer, text, color, duration, soundLoc);
        }
    }

    public static void addToast(Player player, String text, ChatFormatting color, int duration, @Nullable ResourceLocation sound) {
        if (player instanceof ServerPlayer serverPlayer) {
            addToast(serverPlayer, text, color, duration, sound);
        }
    }

    private static void addToast(ServerPlayer player, String text, ChatFormatting color, int duration, @Nullable ResourceLocation soundId) {
        PacketDistributor.sendToPlayer(player, new ToastPacket(text, color.getName(), duration, Optional.ofNullable(soundId)));
    }
}
