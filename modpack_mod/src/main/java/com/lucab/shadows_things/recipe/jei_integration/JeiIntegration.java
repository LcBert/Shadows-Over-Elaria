package com.lucab.shadows_things.recipe.jei_integration;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.client.screen.CauldronScreen;
import com.lucab.shadows_things.client.screen.DryingRackScreen;
import com.lucab.shadows_things.client.screen.OvenScreen;
import com.lucab.shadows_things.client.screen.SmelteryScreen;
import com.lucab.shadows_things.content.block.cauldron.CauldronRegister;
import com.lucab.shadows_things.content.block.drying_rack.DryingRackRegister;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarRegistry;
import com.lucab.shadows_things.content.block.smeltery.SmelteryRegister;
import com.lucab.shadows_things.menus.*;
import com.lucab.shadows_things.recipe.*;
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
        registration.addRecipeCategories(new CauldronRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ResonantAltarRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DryingRackRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new CarcassRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
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

            List<CauldronRecipe> cauldronRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.CAULDRON_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            List<ResonantAltarRecipe> resonantAltarRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.RESONANT_ALTAR_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            List<DryingRackRecipe> dryingRackRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.DRYING_RACK_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            List<CarcassCuttingRecipe> carcassRecipes = recipeManager.getAllRecipesFor(RecipesRegistries.CARCASS_CUTTING_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .toList();

            registration.addRecipes(OvenRecipeCategory.TYPE, ovenRecipes);
            registration.addRecipes(SmelteryRecipeCategory.TYPE, smelteryRecipes);
            registration.addRecipes(CauldronRecipeCategory.TYPE, cauldronRecipes);
            registration.addRecipes(ResonantAltarRecipeCategory.TYPE, resonantAltarRecipes);
            registration.addRecipes(DryingRackRecipeCategory.TYPE, dryingRackRecipes);
            registration.addRecipes(CarcassRecipeCategory.TYPE, carcassRecipes);
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
        registration.addRecipeCatalyst(new ItemStack(CauldronRegister.CAULDRON.get()), CauldronRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(CauldronRegister.CAULDRON.get()), RecipeTypes.FUELING);
        registration.addRecipeCatalyst(new ItemStack(ResonantAltarRegistry.RESONANT_ALTAR.get()), ResonantAltarRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(DryingRackRegister.DRYING_RACK_BLOCK.get()), DryingRackRecipeCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(OvenScreen.class, 43, 37, 64, 22, OvenRecipeCategory.TYPE);
        registration.addRecipeClickArea(SmelteryScreen.class, 69, 33, 20, 25, SmelteryRecipeCategory.TYPE);
        registration.addRecipeClickArea(SmelteryScreen.class, 109, 36, 20, 25, SmelteryRecipeCategory.TYPE);
        registration.addRecipeClickArea(CauldronScreen.class, 120, 27, 22, 15, CauldronRecipeCategory.TYPE);
        registration.addRecipeClickArea(DryingRackScreen.class, 44, 39, 90, 25, DryingRackRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(OvenMenu.class, MenuRegistries.OVEN_MENU.get(), OvenRecipeCategory.TYPE, 0, 3, 7, 36);
        registration.addRecipeTransferHandler(SmelteryMenu.class, MenuRegistries.SMELTERY_MENU.get(), SmelteryRecipeCategory.TYPE, 0, 9, 12, 36);
        registration.addRecipeTransferHandler(CauldronMenu.class, MenuRegistries.CAULDRON_MENU.get(), CauldronRecipeCategory.TYPE, 0, 6, 7, 36);
        registration.addRecipeTransferHandler(DryingRackMenu.class, MenuRegistries.DRYING_RACK_MENU.get(), DryingRackRecipeCategory.TYPE, 0, 5, 10, 36);
    }
}
