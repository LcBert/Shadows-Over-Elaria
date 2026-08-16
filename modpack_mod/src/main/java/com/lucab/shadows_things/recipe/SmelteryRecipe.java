package com.lucab.shadows_things.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SmelteryRecipe implements Recipe<SingleItemRecipeInput> {
    private final SizedIngredient ingredient;
    private final ItemStack result;
    private final int processTime;
    private final int tier;

    public SmelteryRecipe(SizedIngredient ingredient, ItemStack result, int processTime, int tier) {
        this.ingredient = ingredient;
        this.result = result;
        this.processTime = processTime;
        this.tier = tier;
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
    public RecipeSerializer<?> getSerializer() {
        return RecipesRegistries.SMELTERY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.SMELTERY_TYPE.get();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    public SizedIngredient getIngredient() {
        return this.ingredient;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public int getProcessTime() {
        return this.processTime;
    }

    public int getTier() {
        return this.tier;
    }

    public int getIngredientCount() {
        return this.ingredient.count();
    }

    public static class Serializer implements RecipeSerializer<SmelteryRecipe> {
        public static final MapCodec<SmelteryRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(SmelteryRecipe::getIngredient),
                ItemStack.CODEC.fieldOf("result").forGetter(SmelteryRecipe::getResult),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(SmelteryRecipe::getProcessTime),
                Codec.INT.optionalFieldOf("tier", 1).forGetter(SmelteryRecipe::getTier)
        ).apply(inst, SmelteryRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static void toNetwork(RegistryFriendlyByteBuf buf, SmelteryRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buf, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeVarInt(recipe.processTime);
            buf.writeVarInt(recipe.tier);
        }

        private static SmelteryRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            SizedIngredient ingredient = SizedIngredient.STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int cookingTime = buf.readVarInt();
            int tier = buf.readVarInt();
            return new SmelteryRecipe(ingredient, result, cookingTime, tier);
        }

        @Override
        public MapCodec<SmelteryRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }

    public static class RecipeInstance {
        public final int slot;
        public final ItemStack inputStack;
        public final ItemStack outputStack;
        public final int processTime;
        public final int tier;

        public RecipeInstance(int slot, ItemStack inputStack, ItemStack outputStack, int processTime, int tier) {
            this.slot = slot;
            this.inputStack = inputStack;
            this.outputStack = outputStack;
            this.processTime = processTime;
            this.tier = tier;
        }
    }

    public static SmelteryRecipe getRecipe(@NotNull Level level, ItemStack inputStack) {
        SingleItemRecipeInput inputWrapper = new SingleItemRecipeInput(inputStack);
        Optional<RecipeHolder<SmelteryRecipe>> match = level.getRecipeManager().getRecipeFor(RecipesRegistries.SMELTERY_TYPE.get(), inputWrapper, level);
        return match.map(RecipeHolder::value).orElse(null);
    }
}