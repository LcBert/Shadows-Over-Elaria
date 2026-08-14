package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.client.screen.OvenScreen;
import com.lucab.shadows_things.client.screen.SmelteryScreen;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.smeltery.SmelteryRegister;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.menus.OvenMenu;
import com.lucab.shadows_things.menus.SmelteryMenu;
import com.lucab.shadows_things.recipe.OvenRecipe;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.lucab.shadows_things.recipe.SmelteryRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
        registration.addRecipeCategories(new SmelteryRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
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

            List<SmelteryRecipe> smelteryRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.SMELTERY_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(OvenRecipeCategory.TYPE, ovenRecipes);
            registration.addRecipes(SmelteryRecipeCategory.TYPE, smelteryRecipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(OvenRegister.OVEN_BlOCK.get()), OvenRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(OvenRegister.OVEN_BlOCK.get()), RecipeTypes.FUELING);
        SmelteryRegister.getSmelteries().forEach(smelter -> {
            registration.addRecipeCatalyst(new ItemStack(smelter), SmelteryRecipeCategory.TYPE);
            registration.addRecipeCatalyst(new ItemStack(smelter), RecipeTypes.FUELING);
        });
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(OvenScreen.class, 43, 37, 64, 22, OvenRecipeCategory.TYPE);
        registration.addRecipeClickArea(SmelteryScreen.class, 131, 32, 20, 25, SmelteryRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(OvenMenu.class, MenuRegistries.OVEN_MENU.get(), OvenRecipeCategory.TYPE, 0, 3, 7, 36);
        registration.addRecipeTransferHandler(SmelteryMenu.class, MenuRegistries.SMELTERY_MENU.get(), SmelteryRecipeCategory.TYPE, 0, 18, 19, 37);
    }
}
