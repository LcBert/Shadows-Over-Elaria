package com.lucab.shadows_things;

import com.lucab.shadows_things.block.repair_table.RepairType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static boolean isRepairKit(ItemStack stack) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "repair_kits")));
    }

    public static boolean isRepairHammer(ItemStack stack) {
        return stack
                .is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "repair_hammers")));
    }

    public static boolean isItemToRepair(ItemStack stack) {
        for (RepairType value : RepairType.values()) {
            Item valueItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value.item));
            if (stack.getItem() == valueItem)
                return true;
        }
        return false;
    }
}
