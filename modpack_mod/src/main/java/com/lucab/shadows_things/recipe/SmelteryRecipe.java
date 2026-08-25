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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

public class SmelteryRecipe implements Recipe<SingleItemRecipeInput> {
    private final SizedIngredient ingredient;
    private final Ingredient die;
    private final ItemStack result;
    private final boolean consumeDie;
    private final int processTime;
    private final int tier;

    public SmelteryRecipe(SizedIngredient ingredient, Ingredient die, ItemStack result, boolean consumeDie, int processTime, int tier) {
        this.ingredient = ingredient;
        this.die = die;
        this.result = result;
        this.consumeDie = consumeDie;
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

    public Ingredient getDie() {
        return this.die;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public boolean isConsumeDie() {
        return this.consumeDie;
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
                Ingredient.CODEC.fieldOf("die").forGetter(SmelteryRecipe::getDie),
                ItemStack.CODEC.fieldOf("result").forGetter(SmelteryRecipe::getResult),
                Codec.BOOL.optionalFieldOf("consume_die", false).forGetter(SmelteryRecipe::isConsumeDie),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(SmelteryRecipe::getProcessTime),
                Codec.INT.optionalFieldOf("tier", 1).forGetter(SmelteryRecipe::getTier)
        ).apply(inst, SmelteryRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static void toNetwork(RegistryFriendlyByteBuf buf, SmelteryRecipe recipe) {
            SizedIngredient.STREAM_CODEC.encode(buf, recipe.ingredient);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.die);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeBoolean(recipe.consumeDie);
            buf.writeVarInt(recipe.processTime);
            buf.writeVarInt(recipe.tier);
        }

        private static SmelteryRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            SizedIngredient ingredient = SizedIngredient.STREAM_CODEC.decode(buf);
            Ingredient die = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            boolean consumeDie = buf.readBoolean();
            int processTime = buf.readVarInt();
            int tier = buf.readVarInt();
            return new SmelteryRecipe(ingredient, die, result, consumeDie, processTime, tier);
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
        public final ItemStack dieStack;
        public final ItemStack outputStack;
        public final boolean consumeDie;
        public final int processTime;
        public final int tier;

        public RecipeInstance(int slot, ItemStack inputStack, ItemStack dieStack, ItemStack outputStack, boolean consumeDie, int processTime, int tier) {
            this.slot = slot;
            this.inputStack = inputStack;
            this.dieStack = dieStack;
            this.outputStack = outputStack;
            this.consumeDie = consumeDie;
            this.processTime = processTime;
            this.tier = tier;
        }
    }

    public static SmelteryRecipe getRecipe(@NotNull Level level, ItemStack inputStack, ItemStack dieStack) {
        for (RecipeHolder<SmelteryRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipesRegistries.SMELTERY_TYPE.get())) {
            SmelteryRecipe recipe = holder.value();
            if (recipe.ingredient.test(inputStack) && recipe.die.test(dieStack)) {
                return recipe;
            }
        }
        return null;
    }
}