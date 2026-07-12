package com.lucab.shadows_things.rpg.professions;

import net.minecraft.world.entity.player.Player;

public class ProfessionHelper {
    public static ProfessionAttachments getProfessionData(Player player) {
        return player.getData(ProfessionAttachments.PROFESSION.get());
    }

    public static void setLevel(Player player, Professions professions, int level) {
        getProfessionData(player).professionLevels.put(professions, level);
    }

    public static boolean incrementLevel(Player player, Professions profession, int level) {
        ProfessionAttachments data = getProfessionData(player);
        int currentLevel = getLevel(player, profession);
        if (currentLevel >= 5) return false;
        data.professionLevels.put(profession, currentLevel + level);
        return true;
    }

    public static void resetLevel(Player player) {
        for (Professions professions : Professions.values()) {
            setLevel(player, professions, 0);
        }
    }

    public static void resetLevel(Player player, Professions profession) {
        setLevel(player, profession, 0);
    }

    public static int getLevel(Player player, Professions profession) {
        return getProfessionData(player).professionLevels.getOrDefault(profession, 0);
    }

    public static void setPoints(Player player, int points) {
        getProfessionData(player).points = Math.clamp(points, 0, 5);
    }

    public static void addPoints(Player player, int points) {
        setPoints(player, getPoints(player) + points);
    }

    public static void removePoints(Player player, int points) {
        setPoints(player, getPoints(player) - points);
    }

    public static int getPoints(Player player) {
        return getProfessionData(player).points;
    }
}
