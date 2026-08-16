package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class GlassBottles {
    public static final DeferredItem<Item> SPLASH_GLASS_BOTTLE = ShadowsThings.ITEMS.register(
            "splash_glass_bottle", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LINGERING_GLASS_BOTTLE = ShadowsThings.ITEMS.register(
            "lingering_glass_bottle", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SPLASH_GLASS_BOTTLE.get()),
                new ItemStack(LINGERING_GLASS_BOTTLE.get())
        );
    }
}
