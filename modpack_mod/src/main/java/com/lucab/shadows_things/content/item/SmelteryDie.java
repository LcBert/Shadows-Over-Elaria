package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class SmelteryDie {
    public static DeferredItem<Item> SAND_BASE_DIE = ShadowsThings.ITEMS.register("sand_base_die", () -> new Item(new Item.Properties()));
    public static DeferredItem<Item> SAND_INGOT_DIE = ShadowsThings.ITEMS.register("sand_ingot_die", () -> new Item(new Item.Properties()));
    public static DeferredItem<Item> SAND_PLATE_DIE = ShadowsThings.ITEMS.register("sand_plate_die", () -> new Item(new Item.Properties()));
    public static DeferredItem<Item> SAND_ROD_DIE = ShadowsThings.ITEMS.register("sand_rod_die", () -> new Item(new Item.Properties()));

    public static DeferredItem<Item> FORGED_BASE_DIE = ShadowsThings.ITEMS.register("forged_base_die", () -> new Item(new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> FORGED_INGOT_DIE = ShadowsThings.ITEMS.register("forged_ingot_die", () -> new Item(new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> FORGED_PLATE_DIE = ShadowsThings.ITEMS.register("forged_plate_die", () -> new Item(new Item.Properties().stacksTo(1)));
    public static DeferredItem<Item> FORGED_ROD_DIE = ShadowsThings.ITEMS.register("forged_rod_die", () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SAND_BASE_DIE.get()),
                new ItemStack(SAND_INGOT_DIE.get()),
                new ItemStack(SAND_PLATE_DIE.get()),
                new ItemStack(SAND_ROD_DIE.get()),
                new ItemStack(FORGED_BASE_DIE.get()),
                new ItemStack(FORGED_INGOT_DIE.get()),
                new ItemStack(FORGED_PLATE_DIE.get()),
                new ItemStack(FORGED_ROD_DIE.get())
        );
    }
}
