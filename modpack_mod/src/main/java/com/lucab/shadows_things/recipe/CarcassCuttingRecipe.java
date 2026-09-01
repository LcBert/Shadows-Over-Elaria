package com.lucab.shadows_things.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CarcassCuttingRecipe implements Recipe<CarcassCuttingRecipe.CarcassCuttingRecipeInput> {

    public record CarcassCuttingRecipeInput(
            EntityType<?> entityType,
            ItemStack tool,
            int currentStep
    ) implements RecipeInput {
        @Override
        public @NotNull ItemStack getItem(int index) {
            return index == 0 ? this.tool : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    public record StepDefinition(Ingredient tool, ItemStack drop, int minStep, int maxStep) {

        public boolean matches(ItemStack stack, int step) {
            return step >= this.minStep && step <= this.maxStep && this.tool.test(stack);
        }

        public static final Codec<StepDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Ingredient.CODEC.fieldOf("tool").forGetter(StepDefinition::tool),
                ItemStack.CODEC.fieldOf("drop").forGetter(StepDefinition::drop),
                Codec.INT.fieldOf("min_step").forGetter(StepDefinition::minStep),
                Codec.INT.fieldOf("max_step").forGetter(StepDefinition::maxStep)
        ).apply(inst, StepDefinition::new));
    }

    private final String entityType;
    private final int maxInteractions;
    private final NonNullList<StepDefinition> steps;

    public CarcassCuttingRecipe(String entityType, int maxInteractions, List<StepDefinition> steps) {
        this.entityType = entityType;
        this.maxInteractions = maxInteractions;
        this.steps = NonNullList.copyOf(steps);
    }

    public String getEntityTypeString() {
        return entityType;
    }

    @Nullable
    public EntityType<?> getResolvedEntityType() {
        ResourceLocation id = ResourceLocation.tryParse(this.entityType);
        return id != null ? BuiltInRegistries.ENTITY_TYPE.get(id) : null;
    }

    public int getMaxInteractions() {
        return maxInteractions;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public Optional<StepDefinition> getMatchingStep(ItemStack heldTool, int nextStep) {
        for (StepDefinition step : this.steps) {
            if (step.matches(heldTool, nextStep)) {
                return Optional.of(step);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean matches(CarcassCuttingRecipeInput input, Level level) {
        EntityType<?> resolved = this.getResolvedEntityType();
        if (resolved == null || !resolved.equals(input.entityType())) {
            return false;
        }
        if (input.tool().isEmpty()) {
            return true;
        }
        return this.getMatchingStep(input.tool(), input.currentStep()).isPresent();
    }

    @Override
    public ItemStack assemble(CarcassCuttingRecipeInput input, HolderLookup.Provider registries) {
        return this.getMatchingStep(input.tool(), input.currentStep())
                .map(step -> step.drop().copy())
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipesRegistries.CARCASS_CUTTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipesRegistries.CARCASS_CUTTING_TYPE.get();
    }

    // --- Serializer ---
    public static class Serializer implements RecipeSerializer<CarcassCuttingRecipe> {

        public static final MapCodec<CarcassCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.fieldOf("entity_type").forGetter(CarcassCuttingRecipe::getEntityTypeString),
                Codec.INT.fieldOf("max_interactions").forGetter(CarcassCuttingRecipe::getMaxInteractions),
                StepDefinition.CODEC.listOf().fieldOf("steps").forGetter(CarcassCuttingRecipe::getSteps)
        ).apply(inst, CarcassCuttingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CarcassCuttingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static void toNetwork(RegistryFriendlyByteBuf buf, CarcassCuttingRecipe recipe) {
            buf.writeUtf(recipe.entityType);
            buf.writeInt(recipe.maxInteractions);
            buf.writeInt(recipe.steps.size());
            for (StepDefinition step : recipe.steps) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, step.tool);
                ItemStack.STREAM_CODEC.encode(buf, step.drop);
                buf.writeInt(step.minStep);
                buf.writeInt(step.maxStep);
            }
        }

        private static CarcassCuttingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String entityType = buf.readUtf();
            int maxInteractions = buf.readInt();
            int size = buf.readInt();
            NonNullList<StepDefinition> steps = NonNullList.createWithCapacity(size);
            for (int i = 0; i < size; i++) {
                Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ItemStack drop = ItemStack.STREAM_CODEC.decode(buf);
                int minStep = buf.readInt();
                int maxStep = buf.readInt();
                steps.add(new StepDefinition(tool, drop, minStep, maxStep));
            }
            return new CarcassCuttingRecipe(entityType, maxInteractions, steps);
        }

        @Override
        public MapCodec<CarcassCuttingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CarcassCuttingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    @Nullable
    public static RecipeManager getRecipeManager(@Nullable Level level) {
        if (level != null) {
            return level.getRecipeManager();
        }
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getRecipeManager();
        }
        return null;
    }

    public static List<RecipeHolder<CarcassCuttingRecipe>> getAllRecipes(@Nullable Level level) {
        RecipeManager manager = getRecipeManager(level);
        if (manager == null) {
            return Collections.emptyList();
        }
        return manager.getAllRecipesFor(RecipesRegistries.CARCASS_CUTTING_TYPE.get());
    }

    public static Optional<CarcassCuttingRecipe> getRecipeForEntity(@Nullable Level level, EntityType<?> entityType) {
        return getAllRecipes(level).stream()
                .map(RecipeHolder::value)
                .filter(recipe -> entityType.equals(recipe.getResolvedEntityType()))
                .findFirst();
    }

    public static Optional<CarcassCuttingRecipe.StepDefinition> getMatchingStep(
            @Nullable Level level,
            EntityType<?> entityType,
            ItemStack tool,
            int step
    ) {
        return getRecipeForEntity(level, entityType).flatMap(recipe -> recipe.getMatchingStep(tool, step));
    }

    public static int getMaxInteractions(@Nullable Level level, EntityType<?> entityType) {
        return getRecipeForEntity(level, entityType)
                .map(CarcassCuttingRecipe::getMaxInteractions)
                .orElse(0);
    }

    public static boolean hasRecipe(@Nullable Level level, EntityType<?> entityType) {
        return getRecipeForEntity(level, entityType).isPresent();
    }
}