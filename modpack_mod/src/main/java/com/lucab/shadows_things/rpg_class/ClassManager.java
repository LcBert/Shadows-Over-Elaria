package com.lucab.shadows_things.rpg_class;

import net.minecraft.world.entity.player.Player;

public class ClassManager {
    public enum RPGClass {
        WANDERER,
        WARRIOR;
    }

    public static void setClass(Player player, String rpg_class, int tier) throws IllegalArgumentException {
        RPGClass rpgClass = ClassManager.RPGClass.valueOf(rpg_class.toUpperCase());
        setClass(player, rpgClass, tier);
    }

    public static void setClass(Player player, RPGClass rpg_class, int tier) {
        removeClass(player);
        player.getTags().add(String.format("shadow_tags/class/%s/%d", rpg_class.name(), tier));
    }

    public static void resetClass(Player player) {
        setClass(player, RPGClass.WANDERER, 0);
    }

    public static void removeClass(Player player) {
        player.getTags().removeIf(tag -> tag.startsWith("shadow_tags/class/"));
    }

    public static boolean hasClass(Player player) {
        return player.getTags().stream().filter(tag -> tag.startsWith("shadow_tags/class/")).findFirst().orElse(null) != null;
    }

    public static String getClassName(Player player) {
        return player.getTags().stream()
                .filter(tag -> tag.startsWith("shadow_tags/class/"))
                .findFirst()
                .map(tag -> {
                    String[] parts = tag.split("/");
                    return String.format("%s", parts[2].toUpperCase());
                })
                .orElse("none");
    }

    public static int getTier(Player player) {
        return player.getTags().stream()
                .filter(tag -> tag.startsWith("shadow_tags/class/"))
                .findFirst()
                .map(tag -> {
                    String[] parts = tag.split("/");
                    return Integer.parseInt(parts[3]);
                })
                .orElse(-1);
    }

    public static RPGClass getClass(Player player) {
        String className = getClassName(player);
        return RPGClass.valueOf(className);
    }

    public static boolean is(Player player, RPGClass rpgClass) {
        return getClass(player) == rpgClass;
    }
}
