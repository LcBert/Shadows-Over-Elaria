package com.lucab.shadows_things.rpg_class;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.player.Player;
import java.util.Optional;

public class ClassManager {

    public static final String WANDERER = "wanderer";

    public static void setClass(Player player, String rpgClass, int tier) throws IllegalArgumentException {
        String formattedClass = rpgClass.toLowerCase();

        if (!formattedClass.equals(WANDERER)) {
            Optional<RpgClassDataReader.RpgClassData> classData = ShadowsThings.RPG_READER.getClassData(formattedClass);
            if (classData.isEmpty()) {
                throw new IllegalArgumentException("The class " + rpgClass + " does not exist in the datapacks.");
            }
            if (tier < 1 || tier > 5) {
                throw new IllegalArgumentException("Invalid tier. It must be between 1 and 5.");
            }
        } else {
            tier = 0;
        }

        removeClass(player);
        player.getTags().add(String.format("shadow_tags/class/%s/%d", formattedClass, tier));
    }

    public static void resetClass(Player player) {
        setClass(player, WANDERER, 0);
    }

    public static void removeClass(Player player) {
        player.getTags().removeIf(tag -> tag.startsWith("shadow_tags/class/"));
    }

    public static boolean hasClass(Player player) {
        return player.getTags().stream().anyMatch(tag -> tag.startsWith("shadow_tags/class/"));
    }

    public static String getClassName(Player player) {
        return player.getTags().stream()
                .filter(tag -> tag.startsWith("shadow_tags/class/"))
                .findFirst()
                .map(tag -> tag.split("/")[2].toLowerCase())
                .orElse("none");
    }

    public static int getTier(Player player) {
        return player.getTags().stream()
                .filter(tag -> tag.startsWith("shadow_tags/class/"))
                .findFirst()
                .map(tag -> Integer.parseInt(tag.split("/")[3]))
                .orElse(-1);
    }

    public static boolean is(Player player, String rpgClass) {
        return getClassName(player).equals(rpgClass.toLowerCase());
    }
}