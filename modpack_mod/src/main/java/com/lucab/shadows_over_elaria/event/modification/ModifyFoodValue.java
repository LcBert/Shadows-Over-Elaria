package com.lucab.shadows_over_elaria.event.modification;

import java.util.HashMap;
import java.util.Map;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(modid = ShadowsOverElaria.MODID)
public class ModifyFoodValue {
    public static Map<String, Integer> foodValues = new HashMap<>() {
        {
            put("minecraft:carrot", 2);
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
