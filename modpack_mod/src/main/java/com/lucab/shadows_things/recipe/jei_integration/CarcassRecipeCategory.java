package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.item.CarcassItem;
import com.lucab.shadows_things.recipe.CarcassCuttingRecipe;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CarcassRecipeCategory implements IRecipeCategory<CarcassCuttingRecipe> {
    public static final RecipeType<CarcassCuttingRecipe> TYPE = RecipeType.create(
            ShadowsThings.MODID, "carcass_cutting", CarcassCuttingRecipe.class
    );

    private static final int GUI_WIDTH = 150;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 28;
    private static final int BOTTOM_PADDING = 4;
    private static final int DEFAULT_FALLBACK_STEPS = 3;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final Component title;
    private final int calculatedHeight;

    public CarcassRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CarcassItem.createForId("minecraft:pig"));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
        this.title = Component.translatable("jei.shadows_things.recipe.carcass_cutting");

        int maxSteps = calculateMaxSteps();
        this.calculatedHeight = HEADER_HEIGHT + (maxSteps * ROW_HEIGHT) + BOTTOM_PADDING;
    }

    private static int calculateMaxSteps() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            return level.getRecipeManager()
                    .getAllRecipesFor(RecipesRegistries.CARCASS_CUTTING_TYPE.get())
                    .stream()
                    .mapToInt(recipeHolder -> recipeHolder.value().getSteps().size())
                    .max()
                    .orElse(DEFAULT_FALLBACK_STEPS);
        }
        return DEFAULT_FALLBACK_STEPS;
    }

    @Override
    public @NotNull RecipeType<CarcassCuttingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return GUI_WIDTH;
    }

    @Override
    public int getHeight() {
        return this.calculatedHeight;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CarcassCuttingRecipe recipe, IFocusGroup focuses) {
        EntityType<?> resolvedType = recipe.getResolvedEntityType();
        ItemStack inputCarcass = (resolvedType != null) ? CarcassItem.createForType(resolvedType, 0) : ItemStack.EMPTY;

        // Input Carcass Slot
        builder.addSlot(RecipeIngredientRole.INPUT, 7, HEADER_HEIGHT + 1)
                .addItemStack(inputCarcass);

        // Step Slots: Tool Catalyst and Drop Output
        int startY = HEADER_HEIGHT;
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            CarcassCuttingRecipe.StepDefinition step = recipe.getSteps().get(i);
            int rowY = startY + (i * ROW_HEIGHT);

            List<ItemStack> matchingTools = Arrays.asList(step.tool().getItems());

            builder.addSlot(RecipeIngredientRole.CATALYST, 45, rowY + 1)
                    .addItemStacks(matchingTools);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 115, rowY + 1)
                    .addItemStack(step.drop());
        }
    }

    @Override
    public void draw(CarcassCuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        EntityType<?> type = recipe.getResolvedEntityType();
        String name = (type != null) ? type.getDescription().getString() : recipe.getEntityTypeString();

        Component header = Component.literal("§6" + name + " §8(Max: §e" + recipe.getMaxInteractions() + "§8)");
        guiGraphics.drawString(font, header, 6, 3, 0x404040, false);

        this.slot.draw(guiGraphics, 6, HEADER_HEIGHT);

        int startY = HEADER_HEIGHT;
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            CarcassCuttingRecipe.StepDefinition step = recipe.getSteps().get(i);
            int rowY = startY + (i * ROW_HEIGHT);

            this.slot.draw(guiGraphics, 44, rowY);
            this.slot.draw(guiGraphics, 114, rowY);
            this.arrow.draw(guiGraphics, 76, rowY + 1);

            Component stepLabel = Component.literal("§7[§e" + step.minStep() + "§7-§e" + step.maxStep() + "§7]");
            guiGraphics.drawString(font, stepLabel, 77, rowY - 5, 0x555555, false);
        }
    }
}