package com.lucab.shadows_things.recipe;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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

    // Oven
    public static final DeferredHolder<RecipeSerializer<?>, OvenRecipe.Serializer> OVEN_SERIALIZER =
            SERIALIZERS.register("oven_cooking", OvenRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<OvenRecipe>> OVEN_TYPE =
            TYPES.register("oven_cooking", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "oven_cooking")));

    // Smeltery
    public static final DeferredHolder<RecipeSerializer<?>, SmelteryRecipe.Serializer> SMELTERY_SERIALIZER =
            SERIALIZERS.register("smeltery", SmelteryRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SmelteryRecipe>> SMELTERY_TYPE =
            TYPES.register("smeltery", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "smeltery")));

    // Cauldron
    public static final DeferredHolder<RecipeSerializer<?>, CauldronRecipe.Serializer> CAULDRON_SERIALIZER =
            SERIALIZERS.register("cauldron", CauldronRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CauldronRecipe>> CAULDRON_TYPE =
            TYPES.register("cauldron", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "cauldron")));

    // Drying Rack
    public static final DeferredHolder<RecipeSerializer<?>, DryingRackRecipe.Serializer> DRYING_RACK_SERIALIZER =
            SERIALIZERS.register("drying_rack", DryingRackRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRackRecipe>> DRYING_RACK_TYPE =
            TYPES.register("drying_rack", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "drying_rack")));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
