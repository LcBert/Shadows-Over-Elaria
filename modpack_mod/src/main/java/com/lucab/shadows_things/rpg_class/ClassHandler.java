package com.lucab.shadows_things.rpg_class;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ClassHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!ClassManager.hasClass(player))
            ClassManager.resetClass(player);
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!ClassManager.hasClass(player)) return;

        Item heldItem = player.getMainHandItem().getItem();
        ClassManager.RPGClass playerClass = ClassManager.getClass(player);
        int playerTier = ClassManager.getTier(player);

        if (!ItemsManager.isCorrectItem(heldItem, player)) {
            event.setCanceled(true);
        }
    }
}
