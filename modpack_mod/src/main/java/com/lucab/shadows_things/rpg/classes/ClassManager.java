package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ClassManager {

    public static void setClass(Player player, String rpgClass, int tier) throws IllegalArgumentException {
        String formattedClass = rpgClass.toLowerCase();

        // Check if class exists and tier is within 1 and 5
        if (!formattedClass.equals(ClassPlayerData.NONE_CLASS) && !formattedClass.equals(ClassPlayerData.WANDERER_CLASS)) {
            ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(formattedClass);
            if (classData == null) {
                throw new IllegalArgumentException("The class " + rpgClass + " does not exist in the datapacks.");
            }
            if (tier < 1 || tier > 5) {
                throw new IllegalArgumentException("Invalid tier. It must be between 1 and 5.");
            }
        } else {
            tier = 0;
        }

        ClassPlayerData classPlayerData = ClassPlayerData.getClassData(player);

        classPlayerData.setClassName(formattedClass);
        classPlayerData.setClassTier(tier);
        equipClass(player);
        ClassPlayerData.sync(player);
    }

    public static void setClass(Player player, String rpgClass) throws IllegalArgumentException {
        setClass(player, rpgClass, 1);
    }

    public static void setTier(Player player, int tier) throws IllegalArgumentException {
        setClass(player, getClassName(player), tier);
    }

    public static void equipClass(Player player) {
        if (ClassManager.getTier(player) != 1) return;
        ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(getClassName(player));
        if (classData != null) {
            // Equip armor items
            classData.starterKit().armorItems().forEach((slot, item) -> {
                player.setItemSlot(slot, new ItemStack(item));
            });

            // Give inventory items
            classData.starterKit().inventoryItems().forEach(item -> {
                ItemStack itemStack = item.copy();
                if (!player.getInventory().add(itemStack)) player.drop(itemStack, false);
            });
        }
        ;
    }

    public static void resetClass(Player player) {
        setClass(player, ClassPlayerData.WANDERER_CLASS, 0);
    }

    public static void removeClass(Player player) {
        setClass(player, ClassPlayerData.NONE_CLASS, 0);
    }

    public static boolean hasClass(Player player) {
        return !ClassManager.is(player, ClassPlayerData.NONE_CLASS);
    }

    public static boolean isWandererOrNull(Player player) {
        return ClassManager.is(player, ClassPlayerData.WANDERER_CLASS) || ClassManager.is(player, ClassPlayerData.NONE_CLASS);
    }

    public static String getClassName(Player player) {
        return ClassPlayerData.getClassData(player).getClassName();
    }

    public static int getTier(Player player) {
        return ClassPlayerData.getClassData(player).getClassTier();
    }

    public static boolean is(Player player, String rpgClass) {
        return getClassName(player).equals(rpgClass);
    }

    public static void clampExperience(Player player) {
        ClassPlayerData.getClassData(player).setExperience(Math.clamp(getExperience(player), 0, getExperienceRequired(player)));
    }

    public static void setExperience(Player player, int experience) {
        ClassPlayerData.getClassData(player).setExperience(experience);
        clampExperience(player);
        ClassPlayerData.sync(player);
    }

    public static void addExperience(Player player, int experience) {
        setExperience(player, getExperience(player) + experience);
    }

    public static void removeExperience(Player player, int experience) {
        setExperience(player, getExperience(player) - experience);
    }

    public static void resetExperience(Player player) {
        setExperience(player, 0);
    }

    public static int getExperience(Player player) {
        return ClassPlayerData.getClassData(player).getExperience();
    }

    public static int getExperienceRequired(Player player) {
        int tier = getTier(player);
        if (tier <= 0) return 0;
        return (int) (500 * (Math.pow(tier, 1.6)) + 250 * (tier - 1));
    }

    public static float getExperienceProgress(Player player) {
        return getExperience(player) / (float) ClassManager.getExperienceRequired(player);
    }

    public static void levelUp(Player player) {
        if (getTier(player) == 5) return;
        if (getExperience(player) == getExperienceRequired(player)) {
            setClass(player, getClassName(player), getTier(player) + 1);
            setExperience(player, 0);
        }
    }

    public static void executeAction(Player player, int actionType) {
        if (actionType != 1 && actionType != 0) return;

        Level level = player.level();
        ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(getClassName(player));
        ClassPlayerData classPlayerData = ClassPlayerData.getClassData(player);
        if (classData == null) return;

        List<ClassActions.ActionData> actions = switch (actionType) {
            case 0 -> classData.primaryActions();
            case 1 -> classData.secondaryActions();
            default -> null;
        };

        int cooldown = switch (actionType) {
            case 0 -> classData.primaryActionsCooldown();
            case 1 -> classData.secondaryActionsCooldown();
            default -> -1;
        };

        if (actions == null || actions.isEmpty() || cooldown <= -1) return;

        if (actionType == 0) {
            if (!classPlayerData.canUsePrimary(cooldown, level.getGameTime())) {
                player.displayClientMessage(
                        Component.translatable("message.shadows_thibgs.class.primary_action.on_cooldown").withStyle(ChatFormatting.RED),
                        true
                );
                return;
            } else classPlayerData.setPrimaryLastUseTick(level.getGameTime());
        } else {
            if (!classPlayerData.canUseSecondary(cooldown, level.getGameTime())) {
                player.displayClientMessage(
                        Component.translatable("message.shadows_thibgs.class.secondary_action.on_cooldown").withStyle(ChatFormatting.RED),
                        true
                );
                return;
            } else classPlayerData.setSecondaryLastUseTick(level.getGameTime());
        }

        for (ClassActions.ActionData action : actions) {
            action.execute(player);
        }
    }
}