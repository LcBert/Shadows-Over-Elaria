package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DisableModSpawn {
//    @SubscribeEvent
//    public static void onMobSpawn(FinalizeSpawnEvent event) {
//        if (event.getSpawnType().equals(MobSpawnType.NATURAL)) {
//            event.getEntity().discard();
//            event.setCanceled(true);
//        }
//    }

    @SubscribeEvent
    public static void onEntitySpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() == MobSpawnType.NATURAL) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }
}
