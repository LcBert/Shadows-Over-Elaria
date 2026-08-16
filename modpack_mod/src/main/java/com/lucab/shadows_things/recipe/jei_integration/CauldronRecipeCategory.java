package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.cauldron.CauldronRegister;
import com.lucab.shadows_things.recipe.CauldronRecipe;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CauldronRecipeCategory implements IRecipeCategory<CauldronRecipe> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "cauldron");
    public static final RecipeType<CauldronRecipe> TYPE = new RecipeType<>(ID, CauldronRecipe.class);

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final IDrawableAnimated filledArrow;

    public CauldronRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CauldronRegister.CAULDRON.get()));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
        this.filledArrow = helper.createAnimatedDrawable(helper.getRecipeArrowFilled(), 200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<CauldronRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.shadows_things.recipe.cauldron");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 98;
    }

    @Override
    public int getHeight() {
        return 45;
    }

    @Override
    public void draw(CauldronRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                slot.draw(guiGraphics, col * 18, 9 + row * 18);
            }
        }

        slot.draw(guiGraphics, 80, 20);
        arrow.draw(guiGraphics, 56, 20);
        filledArrow.draw(guiGraphics, 56, 20);

        // Draw Cooking Time String
        float seconds = recipe.getProcessTime() / 20.0f;
        String timeString = seconds + "s";
        if (seconds % 1 == 0) timeString = (int) seconds + "s";

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(timeString);
        int textX = (getWidth() / 2) - (textWidth / 2);
        guiGraphics.drawString(font, timeString, textX, 0, 0xFF555555, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CauldronRecipe recipe, IFocusGroup focuses) {
        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                if (index < recipe.getIngredientsList().size()) {
                    SizedIngredient sizedIngredient = recipe.getIngredientsList().get(index);
                    List<ItemStack> stacks = Arrays.stream(sizedIngredient.ingredient().getItems())
                            .map(stack -> {
                                ItemStack copy = stack.copy();
                                copy.setCount(sizedIngredient.count());
                                return copy;
                            }).toList();

                    builder.addSlot(RecipeIngredientRole.INPUT, (col * 18) + 1, (row * 18) + 10)
                            .addItemStacks(stacks);
                }
                index++;
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 21)
                .addItemStack(recipe.getResultItem(null));
    }
}
