package com.lucab.shadows_things.event.modification;

import java.util.HashMap;
import java.util.Map;

import com.lucab.shadows_things.ShadowsThings;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyBurnTime {
    public static Map<String, Integer> burnValues = new HashMap<>() {
        {
            put("minecraft:coal", 40);
        }
    };

    @SubscribeEvent
    public static void modifyBurnTime(FurnaceFuelBurnTimeEvent event) {
        // Remove burn time
        event.setBurnTime(0);

        // Set custom burn time
        event.setBurnTime(burnValues.get(event.getItemStack().getItem().toString()));
    }
}
