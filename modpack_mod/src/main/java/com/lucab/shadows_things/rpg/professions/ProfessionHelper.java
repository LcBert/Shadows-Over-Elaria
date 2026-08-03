package com.lucab.shadows_things.rpg.professions;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.toast.ToastHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ProfessionHelper {
    public enum Professions {
        COOK, BLACKSMITH, FARMER;
    }

    public static final int MAX_PROFESSION_LEVEL = 10;
    public static final int MAX_POINTS = 30;

    public static class BLACKSMITH_CHANCE {
        public static final float[] repair_efficiency = new float[]{0.0f, 0.25f};
        public static final float[] save_kit = new float[]{0.0f, 0.65f};
        public static final float[] repair_xp = new float[]{10.0f, 100.0f};
    }

    public static class FARMER_CHANCE {
        public static final float[] save_tool = new float[]{0.0f, 0.80f};
        public static final float[] extra_crop_drop = new float[]{0.0f, 0.80f};
        public static final float[] crop_xp = new float[]{10.0f, 100.0f};
        public static final float[] roots_xp = new float[]{5.0f, 60.0f};
    }

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

    public static boolean incrementLevel(Player player, Professions profession) {
        int currentLevel = getLevel(player, profession);
        if (currentLevel >= MAX_PROFESSION_LEVEL) return false;
        setLevel(player, profession, currentLevel + 1);
        removePoints(player, 1);
        return true;
    }

    public static void resetAll(Player player) {
        resetLevel(player);
        setPoints(player, 0);
        setExperience(player, 0);
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

    // Consume experience to add 1 point
    public static boolean tryLevelUp(Player player, boolean notify) {
        int currentXp = getExperience(player);
        int requiredXp = getExperienceRequired(player);

        if (currentXp >= requiredXp) {
            removeExperience(player, requiredXp);
            addPoints(player, 1);
            if (notify) {
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                ToastHelper.addToast(player, "New point available", ChatFormatting.YELLOW.getName(), 100);
            }
            return true;
        }
        return false;
    }

    public static boolean canUpgradeProfession(Player player, Professions profession) {
        int currentLevel = getLevel(player, profession);
        return currentLevel < MAX_PROFESSION_LEVEL && getPoints(player) > 0;
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
        int maxLibero = MAX_POINTS - getUsedPoints(player);
        getProfessionData(player).points = Math.clamp(maxLibero, 0, points);
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

    public static int getUsedPoints(Player player) {
        int used = 0;
        for (Professions profession : Professions.values()) used += getLevel(player, profession);
        return used;
    }

    public static int getTotalPoints(Player player) {
        return getPoints(player) + getUsedPoints(player);
    }

    public static float getPol(float[] range, int level) {
        if (level <= 0) return range[0];
        if (level >= MAX_PROFESSION_LEVEL) return range[1];
        float value = range[0] + (range[1] - range[0]) * ((float) (level) / (MAX_PROFESSION_LEVEL));
        ShadowsThings.LOGGER.info("Pol: {}", value);
        return value;
    }
}
