package com.lucab.shadows_things.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class OvenRecipe implements Recipe<SingleItemRecipeInput> {
    private final SizedIngredient ingredient;
    private final ItemStack result;
    private final int cookingTime;

    public OvenRecipe(SizedIngredient ingredient, ItemStack result, int cookingTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(SingleItemRecipeInput input, Level level) {
        ItemStack itemInSlot = input.getItem(0);
        return this.ingredient.test(itemInSlot);
    }

    @Override
    public ItemStack assemble(SingleItemRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipesRegistries.OVEN_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.OVEN_TYPE.get();
    }

    public SizedIngredient getIngredient() {
        return this.ingredient;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    public int getIngredientCount() {
        return this.ingredient.count();
    }

    public static class Serializer implements RecipeSerializer<OvenRecipe> {
        public static final MapCodec<OvenRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(OvenRecipe::getIngredient),
                ItemStack.CODEC.fieldOf("result").forGetter(OvenRecipe::getResult),
                Codec.INT.optionalFieldOf("cooking_time", 200).forGetter(OvenRecipe::getCookingTime)
        ).apply(inst, OvenRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, OvenRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<OvenRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OvenRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static OvenRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            SizedIngredient ingredient = SizedIngredient.STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int cookingTime = buf.readVarInt();
            return new OvenRecipe(ingredient, result, cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, OvenRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buf, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}
