package com.lucab.shadows_things.toast;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class ToastClientHelper {
    public static int ticks = 0;
    public static ConcurrentLinkedDeque<Toast> pendingToasts = new ConcurrentLinkedDeque<>();

    public static void addToast(String text, ChatFormatting color, int duration) {
        addToast(text, color, duration, Optional.empty());
    }

    public static void addToast(String text, ChatFormatting color, int duration, Optional<ResourceLocation> soundId) {
        pendingToasts.add(new Toast(text, color, duration, soundId));
    }

    public static Toast getCurrentToast() {
        return pendingToasts.peek();
    }

    public static int getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;

        Toast toast = getCurrentToast();
        if (toast == null) {
            ticks = 0;
            return;
        }

        if (ticks == 0) {
            toast.soundId().ifPresent(ToastClientHelper::playSound);
        }

        ticks++;

        if (ticks >= toast.duration()) {
            pendingToasts.poll();
            ticks = 0;
        }
    }

    private static void playSound(ResourceLocation soundLocation) {
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundLocation);
        if (soundEvent != null) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F));
        }
    }
}
