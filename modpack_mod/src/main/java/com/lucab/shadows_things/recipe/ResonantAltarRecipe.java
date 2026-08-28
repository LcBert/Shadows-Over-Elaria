package com.lucab.shadows_things.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResonantAltarRecipe implements Recipe<ResonantAltarRecipe.ResonantAltarRecipeInput> {
    public record ResonantAltarRecipeInput(
            ItemStack ingredient,
            List<ItemStack> reagents,
            List<ItemStack> offerings
    ) implements RecipeInput {
        @Override
        public @NotNull ItemStack getItem(int index) {
            if (index == 0) return ingredient;
            int reagentSize = reagents.size();
            if (index - 1 < reagentSize) {
                return reagents.get(index - 1);
            }
            return offerings.get(index - reagentSize - 1);
        }

        @Override
        public int size() {
            return 1 + reagents.size() + offerings.size();
        }
    }

    private final Ingredient ingredient;
    private final NonNullList<Ingredient> reagents;
    private final NonNullList<Ingredient> offerings;
    private final ItemStack result;
    private final int processTime;

    public ResonantAltarRecipe(
            Ingredient ingredient,
            List<Ingredient> reagents,
            List<Ingredient> offerings,
            ItemStack result,
            int processTime
    ) {
        this.ingredient = ingredient;
        this.reagents = NonNullList.copyOf(reagents);
        this.offerings = NonNullList.copyOf(offerings);
        this.result = result;
        this.processTime = processTime;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public List<Ingredient> getReagents() {
        return reagents;
    }

    public List<Ingredient> getOfferings() {
        return offerings;
    }

    public int getProcessTime() {
        return processTime;
    }

    @Override
    public boolean matches(ResonantAltarRecipeInput input, Level level) {
        if (!this.ingredient.test(input.ingredient())) return false;
        return matchesUnordered(input.reagents(), this.reagents) && matchesUnordered(input.offerings(), this.offerings);
    }

    private boolean matchesUnordered(List<ItemStack> inputStacks, List<Ingredient> recipeIngredients) {
        List<ItemStack> nonEmptyInputs = inputStacks.stream().filter(s -> !s.isEmpty()).toList();
        if (nonEmptyInputs.size() != recipeIngredients.size()) {
            return false;
        }

        boolean[] matched = new boolean[nonEmptyInputs.size()];
        for (Ingredient ingredient : recipeIngredients) {
            boolean found = false;
            for (int i = 0; i < nonEmptyInputs.size(); i++) {
                if (!matched[i] && ingredient.test(nonEmptyInputs.get(i))) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(ResonantAltarRecipeInput input, HolderLookup.Provider registries) {
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
        return RecipesRegistries.RESONANT_ALTAR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.RESONANT_ALTAR_TYPE.get();
    }

    // --- Serializer ---
    public static class Serializer implements RecipeSerializer<ResonantAltarRecipe> {

        public static final MapCodec<ResonantAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ResonantAltarRecipe::getIngredient),
                Ingredient.CODEC.listOf().fieldOf("reagents").forGetter(ResonantAltarRecipe::getReagents),
                Ingredient.CODEC.listOf().fieldOf("offerings").forGetter(ResonantAltarRecipe::getOfferings),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.INT.optionalFieldOf("process_time", 200).forGetter(ResonantAltarRecipe::getProcessTime)
        ).apply(inst, ResonantAltarRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ResonantAltarRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static void toNetwork(RegistryFriendlyByteBuf buf, ResonantAltarRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC).encode(buf, recipe.reagents);
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC).encode(buf, recipe.offerings);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeInt(recipe.processTime);
        }

        private static ResonantAltarRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            NonNullList<Ingredient> reagents = ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            NonNullList<Ingredient> offerings = ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC).decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int processTime = buf.readInt();
            return new ResonantAltarRecipe(ingredient, reagents, offerings, result, processTime);
        }

        @Override
        public MapCodec<ResonantAltarRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ResonantAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
