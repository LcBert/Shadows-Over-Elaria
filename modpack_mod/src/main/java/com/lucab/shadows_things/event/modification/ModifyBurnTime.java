package com.lucab.shadows_things.event.modification;

import java.util.HashMap;
import java.util.Map;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyBurnTime {
    public static Map<String, Integer> burnValues = new HashMap<>();
    static {
        burnValues.put("minecraft:coal", 40);
        burnValues.put("farmersdelight:tree_bark", 100);
    }

    @SubscribeEvent
    public static void modifyBurnTime(FurnaceFuelBurnTimeEvent event) {

        if (event.getBurnTime() > 0) {
            ResourceLocation itemLocation = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
            String itemKey = itemLocation.toString();

            // Remove burn time
            event.setBurnTime(0);
            if (burnValues.containsKey(itemKey)) {
                // Set custom burn time
                event.setBurnTime(burnValues.get(itemKey));
            }
        }
    }
}
