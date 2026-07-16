package com.lucab.shadows_things.event.modification;

import java.util.HashMap;
import java.util.Map;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyFoodValue {
    public static Map<String, Integer> foodValues = new HashMap<>() {
        {
            // Tier 1
            put("minecraft:cooked_beef", 2);
            put("minecraft:cooked_porkchop", 2);
            put("minecraft:cooked_mutton", 2);
            put("minecraft:cooked_chicken", 2);
            put("minecraft:cooked_rabbit", 2);

            // Tier 2
            put("farmersdelight:beef_patty", 4);
            put("farmersdelight:cooked_bacon", 4);
            put("farmersdelight:cooked_mutton_chops", 4);
            put("farmersdelight:cooked_chicken_cuts", 4);
        }
    };

    @SubscribeEvent
    public static void modifyFoodValue(ModifyDefaultComponentsEvent event) {
        // Remove food values to all foods
        event.getAllItems().filter(item -> item.components().has(DataComponents.FOOD)).forEach(item -> {
            event.modify(item, builder -> builder.remove(DataComponents.FOOD));
        });

        // Set custom food values
        foodValues.forEach((item, nutrition) -> {
            Item food = BuiltInRegistries.ITEM.get(ResourceLocation.parse(item));

            FoodProperties newFood = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationModifier(0)
                    .build();

            event.modify(food, builder -> builder.set(DataComponents.FOOD, newFood));
        });
    }
}
