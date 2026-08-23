package com.lucab.shadows_things.event.modification;

import java.util.Map;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyBurnTime {
    public static Map<String, Integer> BURN_VALUES = Map.of(
            "farmersdelight:tree_bark", 200,
            "#minecraft:planks", 200
    );

    @SubscribeEvent
    public static void modifyBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();

        if (event.getBurnTime() > 0) event.setBurnTime(0);

        ResourceLocation itemLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemKey = itemLocation.toString();

        if (BURN_VALUES.containsKey(itemKey)) {
            event.setBurnTime(BURN_VALUES.get(itemKey));
            return;
        }

        for (Map.Entry<String, Integer> entry : BURN_VALUES.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("#")) {
                ResourceLocation tagLocation = ResourceLocation.tryParse(key.substring(1));
                if (tagLocation != null && stack.is(ItemTags.create(tagLocation))) {
                    event.setBurnTime(entry.getValue());
                    break;
                }
            }
        }
    }
}
