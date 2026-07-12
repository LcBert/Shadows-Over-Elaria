package com.lucab.shadows_things.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record SingleItemRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index != 0) throw new IllegalArgumentException("Index must be 0");
        return input;
    }

    @Override
    public int size() {
        return 1;
    }
}
