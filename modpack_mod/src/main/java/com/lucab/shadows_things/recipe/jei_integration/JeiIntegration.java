package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.client.screen.OvenScreen;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.menus.OvenMenu;
import com.lucab.shadows_things.recipe.OvenRecipe;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JeiIntegration implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new OvenRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            RecipeManager recipeManager = level.getRecipeManager();

            List<OvenRecipe> ovenRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.OVEN_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(OvenRecipeCategory.TYPE, ovenRecipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlocksRegister.OVEN_BlOCK.get()), OvenRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlocksRegister.OVEN_BlOCK.get()), RecipeTypes.FUELING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(OvenScreen.class, 43, 37, 64, 22, OvenRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(OvenMenu.class, MenuRegistries.OVEN_MENU.get(), OvenRecipeCategory.TYPE, 0, 3, 7, 36);
    }
}
