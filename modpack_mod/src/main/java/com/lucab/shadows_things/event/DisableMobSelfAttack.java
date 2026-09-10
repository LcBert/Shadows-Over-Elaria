package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DisableMobSelfAttack {
    private DisableMobSelfAttack() {
    }

    @SubscribeEvent
    public static void onMobAttachMob(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        if (attacker == null) return;

        if (attacker instanceof Player) return;
        if (target instanceof Player) return;

        event.setCanceled(true);
    }
}
