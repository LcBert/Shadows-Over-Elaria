package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

public class ClassItemsManager {
    /**
     * Checks if an item is bound to a specific class and a specific tier (or lower).
     */
    public static boolean isCorrectItem(Player player, Item item) {
        // If the item is not registered in ANY class of any datapack, anyone can use it.
        if (!isItemBoundToAnyClass(item)) {
            return true;
        }

        String playerClass = ClassManager.getClassName(player);
        int playerTier = ClassManager.getTier(player);

        // If the player is a wanderer or has no class but the item belongs to a specific class, they cannot use it.
        if (playerClass.equals(ClassPlayerData.NONE_CLASS) || playerClass.equals(ClassPlayerData.WANDERER_CLASS)) {
            return false;
        }

        ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(playerClass);
        if (classData == null) return false;

        // Allows usage if the item is present in the current tier or lower ones.
        for (int tier = playerTier; tier >= 1; tier--) {
            List<Item> allowedItems = classData.tiers().get(tier);
            if (allowedItems != null && allowedItems.contains(item)) {
                return true;
            }
        }

        return false;
    }

    public static int getItemTier(Item item) {
        for (ClassDataReader.ClassData classData : ShadowsThings.CLASS_READER.getAllClasses().values()) {
            for (Map.Entry<Integer, List<Item>> entry : classData.tiers().entrySet()) {
                if (entry.getValue() != null && entry.getValue().contains(item)) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    /**
     * Checks if the item is present within the loaded datapack JSON files.
     */
    private static boolean isItemBoundToAnyClass(Item item) {
        for (ClassDataReader.ClassData classData : ShadowsThings.CLASS_READER.getAllClasses().values()) {
            for (List<Item> tierItems : classData.tiers().values()) {
                if (tierItems.contains(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}