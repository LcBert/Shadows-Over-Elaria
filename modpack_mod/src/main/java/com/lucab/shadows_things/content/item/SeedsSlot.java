package com.lucab.shadows_things.content.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class SeedsSlot {
    public static final List<Item> allowedSeedsPerSlot = List.of(
            Items.WHEAT_SEEDS,
            Crops.CARROT_SEEDS.get(),
            Crops.POTATO_SEEDS.get(),
            Items.BEETROOT_SEEDS,
            Crops.ONION_SEEDS.get(),
            BuiltInRegistries.ITEM.get(ResourceLocation.parse("farmersdelight:cabbage_seeds"))
    );

    public static Item getFilterItemForSlot(int bagSlotIndex) {
        if (bagSlotIndex >= 0 && bagSlotIndex < SeedsSlot.allowedSeedsPerSlot.size()) {
            return SeedsSlot.allowedSeedsPerSlot.get(bagSlotIndex);
        }
        return Items.AIR;
    }
}
