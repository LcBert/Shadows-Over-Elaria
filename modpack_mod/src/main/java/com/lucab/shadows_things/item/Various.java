package com.lucab.shadows_things.item;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class Various {
    public static final DeferredItem<Item> STONE_PEBBLE = ShadowsThings.ITEMS.register("stone_pebble", () -> new Item(new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(STONE_PEBBLE.get())
        );
    }
}
