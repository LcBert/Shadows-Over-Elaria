package com.lucab.shadows_things.rpg.professions;

import com.lucab.shadows_things.toast.ToastHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ProfessionHelper {
    public enum Professions {
        MINER("minecraft:iron_pickaxe"),
        COOK("minecraft:cooked_beef"),
        BLACKSMITH("minecraft:anvil"),
        FARMER("minecraft:iron_hoe");

        private final String iconItemKey;

        Professions(String iconItemKey) {
            this.iconItemKey = iconItemKey;
        }

        public String getIconItemKey() {
            return iconItemKey;
        }
    }

    public static final int MAX_PROFESSION_LEVEL = 10;

    public record Range(float min, float max) {
        public float getPol(int level) {
            if (level <= 0) return min;
            if (level >= MAX_PROFESSION_LEVEL) return max;
            return min + (max - min) * ((float) (level) / (MAX_PROFESSION_LEVEL));
        }
    }

    public static class MINER_CHANCE {
        public static final Range saveTool = new Range(0.0f, 0.80f);
        public static final Range oreXp = new Range(10.0f, 100.0f);
    }

    public static class BLACKSMITH_CHANCE {
        public static final Range repairEfficiency = new Range(0.0f, 0.25f);
        public static final Range saveKit = new Range(0.0f, 0.65f);
        public static final Range repairXp = new Range(10.0f, 100.0f);
    }

    public static class FARMER_CHANCE {
        public static final Range saveTool = new Range(0.0f, 0.80f);
        public static final Range extraCropDrop = new Range(0.0f, 0.80f);
        public static final Range cropXp = new Range(10.0f, 100.0f);
        public static final Range rootsXp = new Range(5.0f, 60.0f);
    }


    // =====================
    // SYNC & DATA RETRIEVAL
    // =====================
    public static void sync(Player player) {
        ProfessionAttachments.sync(player);
    }

    public static ProfessionAttachments getData(Player player) {
        return ProfessionAttachments.get(player);
    }

    // =======
    // GETTERS
    // =======
    public static int getLevel(Player player, ProfessionHelper.Professions profession) {
        return getData(player).getLevel(profession);
    }

    public static int getExperience(Player player, Professions profession) {
        return getData(player).getExperience(profession);
    }

    public static ProfessionAttachments.Progress getProgress(Player player, Professions profession) {
        return getData(player).getProgress(profession);
    }

    public static boolean isMaxLevel(Player player, Professions profession) {
        return getData(player).isMaxLevel(profession);
    }

    public static float getExperienceProgress(Player player, Professions profession) {
        return getData(player).getExperienceProgress(profession);
    }

    public static int getRequiredExperience(Player player, Professions profession) {
        return ProfessionAttachments.getRequiredExpForLevel(getLevel(player, profession));
    }

    public static Map<Professions, ProfessionAttachments.Progress> getAllProfessions(Player player) {
        return getData(player).getProfessionsMap();
    }

    // =============================
    // MUTATORS (EXPERIENCE & LEVEL)
    // =============================
    public static void addExperience(Player player, Professions profession, int amount) {
        if (amount <= 0 || isMaxLevel(player, profession)) {
            return;
        }

        ProfessionAttachments data = getData(player);
        int oldLevel = data.getLevel(profession);

        data.addExperience(profession, amount);
        int newLevel = data.getLevel(profession);

        if (newLevel > oldLevel) {
            onLevelUp(player, profession, newLevel);
        }

        sync(player);
    }

    public static void removeExperience(Player player, Professions profession, int amount) {
        if (amount <= 0) return;
        getData(player).removeExperience(profession, amount);
        sync(player);
    }

    public static void setLevel(Player player, Professions profession, int level) {
        getData(player).setLevel(profession, level);
        sync(player);
    }

    public static void setExperience(Player player, Professions profession, int experience) {
        getData(player).setExperience(profession, experience);
        sync(player);
    }

    public static void setProgress(Player player, Professions profession, int level, int experience) {
        getData(player).setProgress(profession, level, experience);
        sync(player);
    }

    public static void addLevel(Player player, Professions profession, int levels) {
        if (levels <= 0) return;
        ProfessionAttachments data = getData(player);
        int oldLevel = data.getLevel(profession);

        data.addLevel(profession, levels);
        int newLevel = data.getLevel(profession);

        if (newLevel > oldLevel) {
            onLevelUp(player, profession, newLevel);
        }

        sync(player);
    }

    public static void removeLevel(Player player, Professions profession, int levels) {
        if (levels <= 0) return;
        getData(player).removeLevel(profession, levels);
        sync(player);
    }

    public static void resetProfession(Player player, Professions profession) {
        getData(player).resetProfession(profession);
        sync(player);
    }

    public static void resetAll(Player player) {
        getData(player).resetAll();
        sync(player);
    }

    // =========================
    // LEVEL-UP LOGIC & FEEDBACK
    // =========================
    private static void onLevelUp(Player player, Professions profession, int newLevel) {
        if (player.level().isClientSide()) return;

        if (player instanceof ServerPlayer serverPlayer) {
            String professionName = Component.translatable("gui.shadows_things.profession.name." + profession.name().toLowerCase()).getString();
            Component title = Component.translatable("toast.shadows_things.profession.levelup.title", professionName, newLevel);

            ToastHelper.addToast(serverPlayer, title.getString(), ChatFormatting.GREEN.getName(), 200, SoundEvents.PLAYER_LEVELUP);
        }
    }
}
