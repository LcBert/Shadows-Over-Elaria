package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarRegistry;
import com.lucab.shadows_things.recipe.ResonantAltarRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonantAltarRecipeCategory implements IRecipeCategory<ResonantAltarRecipe> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "resonant_altar");
    public static final RecipeType<ResonantAltarRecipe> TYPE = new RecipeType<>(ID, ResonantAltarRecipe.class);

    private static final int GUI_WIDTH = 150;
    private static final int GUI_HEIGHT = 170;

    private static final int CENTER_X = 75;
    private static final int CENTER_Y = 70;
    private static final int HALF_SLOT = 9;

    private static final int INNER_RADIUS = 28;
    private static final int OUTER_RADIUS = 56;

    private static final int OUTPUT_SLOT_X = CENTER_X - HALF_SLOT;
    private static final int OUTPUT_SLOT_Y = GUI_HEIGHT - 18;

    // Coordinate arrays
    private final int[][] reagentCoords;
    private final int[][] offeringCoords;

    private final IDrawable icon;
    private final IDrawable slot;

    public ResonantAltarRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ResonantAltarRegistry.RESONANT_ALTAR.get()));
        this.slot = helper.getSlotDrawable();

        // Calculate 4 inner diagonal reagents (45 deg)
        int innerDiag = (int) Math.round(INNER_RADIUS * Math.cos(Math.PI / 4.0)); // ~20px
        this.reagentCoords = new int[][]{
                {CENTER_X - innerDiag - HALF_SLOT, CENTER_Y - innerDiag - HALF_SLOT}, // NW
                {CENTER_X + innerDiag - HALF_SLOT, CENTER_Y - innerDiag - HALF_SLOT}, // NE
                {CENTER_X + innerDiag - HALF_SLOT, CENTER_Y + innerDiag - HALF_SLOT}, // SE
                {CENTER_X - innerDiag - HALF_SLOT, CENTER_Y + innerDiag - HALF_SLOT}  // SW
        };

        // Calculate 8 outer regular octagon offerings (4 cardinals + 4 diagonals)
        int outerDiag = (int) Math.round(OUTER_RADIUS * Math.cos(Math.PI / 4.0)); // ~40px
        this.offeringCoords = new int[][]{
                {CENTER_X - HALF_SLOT, CENTER_Y - OUTER_RADIUS - HALF_SLOT},              // N
                {CENTER_X + outerDiag - HALF_SLOT, CENTER_Y - outerDiag - HALF_SLOT},      // NE
                {CENTER_X + OUTER_RADIUS - HALF_SLOT, CENTER_Y - HALF_SLOT},              // E
                {CENTER_X + outerDiag - HALF_SLOT, CENTER_Y + outerDiag - HALF_SLOT},      // SE
                {CENTER_X - HALF_SLOT, CENTER_Y + OUTER_RADIUS - HALF_SLOT},              // S
                {CENTER_X - outerDiag - HALF_SLOT, CENTER_Y + outerDiag - HALF_SLOT},      // SW
                {CENTER_X - OUTER_RADIUS - HALF_SLOT, CENTER_Y - HALF_SLOT},              // W
                {CENTER_X - outerDiag - HALF_SLOT, CENTER_Y - outerDiag - HALF_SLOT}       // NW
        };
    }

    @Override
    public RecipeType<ResonantAltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.shadows_things.recipe.resonant_altar");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return GUI_WIDTH;
    }

    @Override
    public int getHeight() {
        return GUI_HEIGHT;
    }

    @Override
    public void draw(@NotNull ResonantAltarRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw standard vanilla JEI slot frames on all positions
        // Center Slot
        this.slot.draw(guiGraphics, CENTER_X - HALF_SLOT, CENTER_Y - HALF_SLOT);

        // Reagents Slots
        for (int[] coord : reagentCoords) {
            this.slot.draw(guiGraphics, coord[0], coord[1]);
        }

        // Offerings Slots
        for (int[] coord : offeringCoords) {
            this.slot.draw(guiGraphics, coord[0], coord[1]);
        }

        // Output Slot Frame
        this.slot.draw(guiGraphics, OUTPUT_SLOT_X, OUTPUT_SLOT_Y);

        // Draw process time info at bottom
        Font font = Minecraft.getInstance().font;
        String timeString = (recipe.getProcessTime() / 20) + "s";
        int textWidth = font.width(timeString);
        guiGraphics.drawString(font, timeString, GUI_WIDTH - textWidth - 6, GUI_HEIGHT - 12, 0x8A8A8A, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ResonantAltarRecipe recipe, @NotNull IFocusGroup focuses) {
        // Center Base Input & Output
        // Adding slot 1px offset to perfectly fit inside vanilla slot drawable border
        builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X - HALF_SLOT + 1, CENTER_Y - HALF_SLOT + 1)
                .addIngredients(recipe.getIngredient());


        // Inner Ring (Reagents)
        List<Ingredient> reagents = recipe.getReagents();
        for (int i = 0; i < reagentCoords.length; i++) {
            var slotBuilder = builder.addSlot(
                    RecipeIngredientRole.INPUT,
                    reagentCoords[i][0] + 1,
                    reagentCoords[i][1] + 1
            );
            if (i < reagents.size()) {
                slotBuilder.addIngredients(reagents.get(i));
            }
        }

        // Outer Ring (Offerings)
        List<Ingredient> offerings = recipe.getOfferings();
        for (int i = 0; i < offeringCoords.length; i++) {
            var slotBuilder = builder.addSlot(
                    RecipeIngredientRole.INPUT,
                    offeringCoords[i][0] + 1,
                    offeringCoords[i][1] + 1
            );
            if (i < offerings.size()) {
                slotBuilder.addIngredients(offerings.get(i));
            }
        }

        // Output Slot Below All Inputs
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X + 1, OUTPUT_SLOT_Y + 1)
                .addItemStack(recipe.getResultItem(null));
    }
}
