package com.lucab.shadows_things.recipe;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipesRegistries {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ShadowsThings.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ShadowsThings.MODID);

    // Registrazione Serializer
    public static final DeferredHolder<RecipeSerializer<?>, OvenRecipe.Serializer> OVEN_SERIALIZER =
            SERIALIZERS.register("oven_cooking", OvenRecipe.Serializer::new);

    // Registrazione Type
    public static final DeferredHolder<RecipeType<?>, RecipeType<OvenRecipe>> OVEN_TYPE =
            TYPES.register("oven_cooking", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "oven_cooking";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
