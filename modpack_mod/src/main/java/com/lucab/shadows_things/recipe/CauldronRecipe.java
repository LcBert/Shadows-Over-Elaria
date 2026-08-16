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
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CauldronRecipe implements Recipe<RecipeInput> {
    private final NonNullList<SizedIngredient> ingredients;
    private final ItemStack output;
    private final int processingTime;

    public CauldronRecipe(NonNullList<SizedIngredient> ingredients, ItemStack output, int processingTime) {
        this.ingredients = ingredients;
        this.output = output;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        for (SizedIngredient sized : this.ingredients) {
            boolean matched = false;
            for (int i = 0; i < input.size(); i++) {
                if (sized.test(input.getItem(i))) {
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipesRegistries.CAULDRON_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.CAULDRON_TYPE.get();
    }

    public int getProcessTime() {
        return this.processingTime;
    }

    public NonNullList<SizedIngredient> getIngredientsList() {
        return this.ingredients;
    }

    public static class Serializer implements RecipeSerializer<CauldronRecipe> {
        public static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.FLAT_CODEC.listOf().fieldOf("ingredients").xmap(
                        list -> {
                            NonNullList<SizedIngredient> nonNullList = NonNullList.create();
                            nonNullList.addAll(list);
                            return nonNullList;
                        },
                        list -> list
                ).forGetter(CauldronRecipe::getIngredientsList),
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.getResultItem(null)),
                Codec.INT.optionalFieldOf("processing_time", 200).forGetter(CauldronRecipe::getProcessTime)
        ).apply(inst, CauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.getIngredientsList(),
                ItemStack.STREAM_CODEC,
                recipe -> recipe.getResultItem(null),
                ByteBufCodecs.INT,
                CauldronRecipe::getProcessTime,
                (ingredients, output, time) -> {
                    NonNullList<SizedIngredient> list = NonNullList.create();
                    list.addAll(ingredients);
                    return new CauldronRecipe(list, output, time);
                }
        );

        @Override
        public MapCodec<CauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record CauldronRecipeInput(List<ItemStack> inputs) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return this.inputs.get(index);
        }

        @Override
        public int size() {
            return this.inputs.size();
        }

        @Override
        public boolean isEmpty() {
            return this.inputs.stream().allMatch(ItemStack::isEmpty);
        }
    }

    public static class RecipeInstance {
        public final Map<Integer, Integer> inputSlotCount;
        public final Map<Integer, ItemStack> stackSlots;
        public final ItemStack outputStack;
        public final int processTime;

        public RecipeInstance(Map<Integer, Integer> inputSlotCount, Map<Integer, ItemStack> stackSlots, ItemStack outputStack, int processTime) {
            this.inputSlotCount = inputSlotCount;
            this.stackSlots = stackSlots;
            this.outputStack = outputStack;
            this.processTime = processTime;
        }
    }

    public static CauldronRecipe getRecipe(@NotNull Level level, List<ItemStack> inputStacks) {
        CauldronRecipeInput inputWrapper = new CauldronRecipeInput(inputStacks);
        Optional<RecipeHolder<CauldronRecipe>> match = level.getRecipeManager().getRecipeFor(RecipesRegistries.CAULDRON_TYPE.get(), inputWrapper, level);
        return match.map(RecipeHolder::value).orElse(null);
    }
}
