package com.lucab.shadows_things.rpg_class;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemsManager {
    public static Map<ClassManager.RPGClass, Map<Integer, List<Item>>> CLASS_ITEMS = new HashMap<>();
    public static List<Item> ALL_ITEMS = new ArrayList<>();

    static {
        // 1. Pre-initialize the maps so they aren't recreated constantly
        for (ClassManager.RPGClass rpgClass : ClassManager.RPGClass.values()) {
            Map<Integer, List<Item>> tierMap = new HashMap<>();
            for (int tier = 1; tier <= 5; tier++) {
                tierMap.put(tier, new ArrayList<>());
            }
            CLASS_ITEMS.put(rpgClass, tierMap);
        }

        // 2. Iterate through items ONCE
        BuiltInRegistries.ITEM.forEach(item -> {
            for (ClassManager.RPGClass rpgClass : ClassManager.RPGClass.values()) {
                for (int tier = 1; tier <= 5; tier++) {
                    // Generate the tag location
                    ResourceLocation tagLoc = ResourceLocation.fromNamespaceAndPath("shadows_things",
                            String.format("class/%s/%d", rpgClass.name().toLowerCase(), tier));

                    // Check if the item has the tag
                    if (item.getDefaultInstance().is(ItemTags.create(tagLoc))) {
                        CLASS_ITEMS.get(rpgClass).get(tier).add(item);

                        // Only add to ALL_ITEMS once per item, even if it fits multiple classes
                        if (!ALL_ITEMS.contains(item)) {
                            ALL_ITEMS.add(item);
                        }
                    }
                }
            }
        });
    }

    public static ClassManager.RPGClass getItemClass(Item item) {
        for (ClassManager.RPGClass rpgClass : ClassManager.RPGClass.values()) {
            for (int tier = 1; tier <= 5; tier++) {
                List<Item> items = CLASS_ITEMS.get(rpgClass).get(tier);
                if (items.contains(item)) {
                    return rpgClass;
                }
            }
        }
        return null;
    }

    public static boolean isCorrectItem(Item item, Player player) {
        if (!ALL_ITEMS.contains(item)) return true;

        ClassManager.RPGClass playerClass = ClassManager.getClass(player);
        int playerTier = ClassManager.getTier(player);
        System.out.println(CLASS_ITEMS.size());
        for (int tier = playerTier; tier >= 1; tier--) {
            if (CLASS_ITEMS.get(playerClass).get(tier).contains(item)) return true;
        }
        return false;
    }
}
