package com.lucab.shadows_things.item;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.neoforge.registries.DeferredItem;

public class CopperTools {

    public static final DeferredItem<PickaxeItem> COPPER_PICKAXE = ShadowsThings.ITEMS.register(
            "copper_pickaxe", () -> new PickaxeItem(ModTiers.COPPER,
                    new Item.Properties().attributes(PickaxeItem.createAttributes(ModTiers.COPPER, 0.0F, -3F))));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                COPPER_PICKAXE.get().getDefaultInstance());
    }
}
