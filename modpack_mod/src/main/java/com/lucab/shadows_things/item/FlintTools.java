package com.lucab.shadows_things.item;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;

public class FlintTools {
    public static final DeferredItem<PickaxeItem> FLINT_PICKAXE = ShadowsThings.ITEMS.register(
            "flint_pickaxe", () -> new PickaxeItem(ModTiers.FLINT,
                    new Item.Properties().attributes(AxeItem.createAttributes(Tiers.WOOD, 0.0F, -3F))));

    public static final DeferredItem<AxeItem> FLINT_AXE = ShadowsThings.ITEMS.register(
            "flint_axe", () -> new AxeItem(ModTiers.FLINT,
                    new Item.Properties().attributes(AxeItem.createAttributes(Tiers.WOOD, 0.0F, -3F))));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                FLINT_PICKAXE.get().getDefaultInstance(),
                FLINT_AXE.get().getDefaultInstance());
    }
}
