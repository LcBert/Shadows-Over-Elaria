package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.smeltery.SmelteryRegister;
import com.lucab.shadows_things.recipe.SmelteryRecipe;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SmelteryRecipeCategory implements IRecipeCategory<SmelteryRecipe> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "smeltery");
    public static final RecipeType<SmelteryRecipe> TYPE = new RecipeType<>(ID, SmelteryRecipe.class);

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final IDrawableAnimated filledArrow;

    public SmelteryRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(SmelteryRegister.STONE_SMELTERY.get()));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
        this.filledArrow = helper.createAnimatedDrawable(helper.getRecipeArrowFilled(), 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<SmelteryRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.shadows_things.recipe.smeltery");
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
        return 45;
    }

    @Override
    public void draw(SmelteryRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Input
        slot.draw(guiGraphics, 0, 10);
        // Die
        slot.draw(guiGraphics, 0, 28);
        // Output
        slot.draw(guiGraphics, 47, 20);

        arrow.draw(guiGraphics, 22, 20);
        filledArrow.draw(guiGraphics, 22, 20);

        int tier = recipe.getTier();
        String tierString = "Tier: " + tier;

        float seconds = recipe.getProcessTime() / 20.0f;
        String timeString = seconds + "s";
        if (seconds % 1 == 0) timeString = (int) seconds + "s";

        String titleString = tierString + " | " + timeString;

        int tierTextX = (getWidth() / 2) - (font.width(titleString) / 2);
        int tierTextY = 0;
        guiGraphics.drawString(font, titleString, tierTextX, tierTextY, 0xFF555555, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmelteryRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 11)
                .addItemStack(new ItemStack(recipe.getIngredient().ingredient().getItems()[0].getItem(), recipe.getIngredientCount()));

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 29)
                .addItemStacks(Arrays.stream(recipe.getDie().getItems()).toList())
                .addRichTooltipCallback(((recipeSlotView, tooltip) -> {
                    if (recipe.isConsumeDie())
                        tooltip.add(Component.literal("Consumed").withStyle(ChatFormatting.RED));
                }));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 21)
                .addItemStack(recipe.getResult());
    }
}
