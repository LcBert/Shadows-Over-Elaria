package com.lucab.shadows_things.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DryingRackRecipe implements Recipe<SingleRecipeInput> {
    private final SizedIngredient ingredient;
    private final ItemStack result;
    private final int processTime;

    public DryingRackRecipe(SizedIngredient ingredient, ItemStack result, int processTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack itemInSlot = input.getItem(0);
        return this.ingredient.test(itemInSlot);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
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
        return RecipesRegistries.DRYING_RACK_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.DRYING_RACK_TYPE.get();
    }

    public SizedIngredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getProcessTime() {
        return processTime;
    }

    public static class Serializer implements RecipeSerializer<DryingRackRecipe> {
        public static final MapCodec<DryingRackRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(DryingRackRecipe::getIngredient),
                        ItemStack.CODEC.fieldOf("result").forGetter(DryingRackRecipe::getResult),
                        Codec.INT.optionalFieldOf("process_time", 200).forGetter(DryingRackRecipe::getProcessTime)
                ).apply(inst, DryingRackRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static void toNetwork(RegistryFriendlyByteBuf buf, DryingRackRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
            buf.writeInt(recipe.getProcessTime());
        }

        private static DryingRackRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            SizedIngredient ingredient = SizedIngredient.STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int processTime = buf.readInt();
            return new DryingRackRecipe(ingredient, result, processTime);
        }

        @Override
        public MapCodec<DryingRackRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingRackRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record RecipeInstance(
            ItemStack inputStack,
            ItemStack outputStack,
            int processTime
    ) {
    }

    public static DryingRackRecipe getRecipe(@NotNull Level level, ItemStack inputStack) {
        for (RecipeHolder<DryingRackRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipesRegistries.DRYING_RACK_TYPE.get())) {
            DryingRackRecipe recipe = holder.value();
            if (recipe.ingredient.test(inputStack)) {
                return recipe;
            }
        }
        return null;
    }
}
