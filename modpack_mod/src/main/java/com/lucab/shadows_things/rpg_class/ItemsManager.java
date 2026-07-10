package com.lucab.shadows_things.rpg_class;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import java.util.List;

public class ItemsManager {

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
        if (playerClass.equals(ClassManager.WANDERER) || playerClass.equals("none")) {
            return false;
        }

        var classDataOpt = ShadowsThings.RPG_READER.getClassData(playerClass);
        if (classDataOpt.isEmpty()) return false;

        RpgClassDataReader.RpgClassData data = classDataOpt.get();

        // Allows usage if the item is present in the current tier or lower ones.
        for (int tier = playerTier; tier >= 1; tier--) {
            List<Item> allowedItems = data.tiers().get(tier);
            if (allowedItems != null && allowedItems.contains(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the item is present within the loaded datapack JSON files.
     */
    private static boolean isItemBoundToAnyClass(Item item) {
        for (RpgClassDataReader.RpgClassData classData : ShadowsThings.RPG_READER.getAllClasses().values()) {
            for (List<Item> tierItems : classData.tiers().values()) {
                if (tierItems.contains(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}