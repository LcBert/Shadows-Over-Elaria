package com.lucab.shadows_things.rpg.professions;

import net.minecraft.world.entity.player.Player;

public class ProfessionHelper {
    public enum Professions {
        COOK, BLACKSMITH, FARMER;
    }

    public static class BLACKSMITH_CHANCE {
        public static final float[] repair_efficiency = new float[]{0.05f, 0.20f};
        public static final float[] save_kit = new float[]{0.10f, 0.65f};
    }

    public static class FARMER_CHANCE {
        public static final float[] save_tool = new float[]{0.15f, 0.80f};
        public static final float[] double_drop = new float[]{0.20f, 0.80f};
    }

    public static final int MAX_PROFESSION_LEVEL = 10;

    private static void sync(Player player) {
        player.setData(ProfessionAttachments.PROFESSION.get(), getProfessionData(player));
    }

    private static ProfessionAttachments getProfessionData(Player player) {
        return player.getData(ProfessionAttachments.PROFESSION.get());
    }

    public static void setLevel(Player player, Professions professions, int level) {
        getProfessionData(player).professionLevels.put(professions, Math.clamp(level, 0, MAX_PROFESSION_LEVEL));
        sync(player);
    }

    public static boolean incrementLevel(Player player, Professions profession, int level) {
        ProfessionAttachments data = getProfessionData(player);
        int currentLevel = getLevel(player, profession);
        if (currentLevel >= MAX_PROFESSION_LEVEL) return false;
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

    public static boolean tryLevelUp(Player player) {
        int currentXp = getExperience(player);
        int requiredXp = getExperienceRequired(player);

        if (currentXp >= requiredXp) {
            removeExperience(player, requiredXp);
            addPoints(player, 1);
            return true;
        }
        return false;
    }

    public static void setExperience(Player player, int xp) {
        getProfessionData(player).experience = Math.max(0, xp);
        sync(player);
    }

    public static void addExperience(Player player, int xp) {
        setExperience(player, getExperience(player) + xp);
    }

    public static void removeExperience(Player player, int xp) {
        setExperience(player, getExperience(player) - xp);
    }

    public static int getExperience(Player player) {
        return getProfessionData(player).experience;
    }

    public static int getExperienceRequired(Player player) {
        int totalPoints = getTotalPoints(player);
        return (int) (500 * Math.pow(1.2, totalPoints));
    }

    public static void setPoints(Player player, int points) {
        getProfessionData(player).points = Math.max(points, 0);
        sync(player);
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

    public static int getTotalPoints(Player player) {
        int total = 0;
        for (Professions profession : Professions.values()) {
            total += ProfessionHelper.getLevel(player, profession);
        }

        total += ProfessionHelper.getPoints(player);
        return total;
    }

    public static float getPol(float[] range, int level) {
        if (level == 0) return 0.0f;
        return range[0] + (range[1] - range[0]) * ((float) (level - 1) / (MAX_PROFESSION_LEVEL - 1));
    }
}
