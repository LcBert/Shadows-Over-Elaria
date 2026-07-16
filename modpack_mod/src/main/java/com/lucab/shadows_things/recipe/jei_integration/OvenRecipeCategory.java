package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.recipe.OvenRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OvenRecipeCategory implements IRecipeCategory<OvenRecipe> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "oven_cooking");
    public static final RecipeType<OvenRecipe> TYPE = new RecipeType<>(ID, OvenRecipe.class);

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final IDrawableAnimated filledArrow;

    public OvenRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlocksRegister.OVEN_BlOCK.get()));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
        this.filledArrow = helper.createAnimatedDrawable(helper.getRecipeArrowFilled(), 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<OvenRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.shadows_things.oven");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 25;
    }

    @Override
    public void draw(OvenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, 0, 0);
        slot.draw(guiGraphics, 47, 0);
        arrow.draw(guiGraphics, 22, 2);
        filledArrow.draw(guiGraphics, 22, 2);

        // Draw Cooking Time String
        float seconds = recipe.getCookingTime() / 20.0f;
        String timeString = seconds + "s";
        if (seconds % 1 == 0) timeString = (int) seconds + "s";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(timeString);

        int textX = (getWidth() / 2) - (textWidth / 2);
        int textY = 19;

        guiGraphics.drawString(font, timeString, textX, textY, 0xFF555555, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OvenRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStack(new ItemStack(recipe.getIngredient().ingredient().getItems()[0].getItem(), recipe.getIngredientCount()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 1)
                .addItemStack(recipe.getResult());
    }
}
