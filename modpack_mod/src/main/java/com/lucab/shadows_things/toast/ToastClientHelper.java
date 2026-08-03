package com.lucab.shadows_things.toast;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.concurrent.ConcurrentLinkedDeque;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class ToastClientHelper {
    public static int ticks = 0;
    public static ConcurrentLinkedDeque<Toast> pendingToasts = new ConcurrentLinkedDeque<>();

    public static void addToast(String text, ChatFormatting color, int duration) {
        pendingToasts.add(new Toast(text, color, duration));
    }

    public static Toast getCurrentToast() {
        return pendingToasts.stream().findFirst().orElse(null);
    }

    public static int getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        Toast toast = getCurrentToast();
        if (toast == null) {
            ticks = 0;
            return;
        }

//        if (ticks == 0) {
//            player.displayClientMessage(Component.literal(toast.text()).withStyle(toast.color()), false);
//        }

        ticks++;

        if (ticks >= toast.duration()) {
            pendingToasts.poll();
            ticks = 0;
        }
    }
}
