package com.lucab.shadows_things.toast;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Optional;

public class ToastHelper {
    public static void addToast(Player player, String text, String color, int duration) {
        addToast(player, text, color, duration, (ResourceLocation) null);
    }

    public static void addToast(ServerPlayer player, String text, String color, int duration) {
        addToast(player, text, color, duration, (ResourceLocation) null);
    }

    public static void addToast(Player player, String text, String color, int duration, @Nullable SoundEvent sound) {
        ResourceLocation soundLoc = sound != null ? BuiltInRegistries.SOUND_EVENT.getKey(sound) : null;
        addToast(player, text, color, duration, soundLoc);
    }

    public static void addToast(Player player, String text, String color, int duration, @Nullable Holder<SoundEvent> soundHolder) {
        ResourceLocation soundLoc = soundHolder != null ? soundHolder.unwrapKey().map(ResourceKey::location).orElse(null) : null;
        addToast(player, text, color, duration, soundLoc);
    }

    public static void addToast(Player player, String text, String color, int duration, @Nullable ResourceLocation soundId) {
        if (player instanceof ServerPlayer serverPlayer) {
            addToast(serverPlayer, text, color, duration, soundId);
        }
    }

    public static void addToast(ServerPlayer player, String text, String color, int duration, @Nullable ResourceLocation soundId) {
        PacketDistributor.sendToPlayer(player, new ToastPacket(text, color, duration, Optional.ofNullable(soundId)));
    }
}
