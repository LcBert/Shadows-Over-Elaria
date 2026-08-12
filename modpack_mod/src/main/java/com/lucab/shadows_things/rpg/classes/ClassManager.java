package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class ClassManager {
    public static final String NULL = "null";
    public static final String WANDERER = "wanderer";

    public static void syncClass(Player player) {
        String className = getClassName(player);
        int tier = getTier(player);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerClassPacket(className, tier));
        }
    }

    public static void setClass(Player player, String rpgClass, int tier) throws IllegalArgumentException {
        String formattedClass = rpgClass.toLowerCase();

        if (!formattedClass.equals(WANDERER) && !formattedClass.equals(NULL)) {
            Optional<ClassDataReader.ClassData> classData = ShadowsThings.CLASS_READER.getClassData(formattedClass);
            if (classData.isEmpty()) {
                throw new IllegalArgumentException("The class " + rpgClass + " does not exist in the datapacks.");
            }
            if (tier < 1 || tier > 5) {
                throw new IllegalArgumentException("Invalid tier. It must be between 1 and 5.");
            }
        } else {
            tier = 0;
        }

        player.getTags().removeIf(tag -> tag.startsWith("shadow_tags/class/"));
        if (!formattedClass.equals(NULL))
            player.getTags().add(String.format("shadow_tags/class/%s/%d", formattedClass, tier));
        ClassModifierApplier.updatePlayerAttributes(player, rpgClass);

        equipClass(player);
        syncClass(player);
    }

    public static void equipClass(Player player) {
        ShadowsThings.CLASS_READER.getClassData(getClassName(player)).ifPresent(classData -> {
            // Equip armor items
            classData.starterKit().armorItems().forEach((slot, item) -> {
                player.setItemSlot(slot, new ItemStack(item));
            });

            // Give inventory items - drop if inventory is full
            classData.starterKit().inventoryItems().forEach(item -> {
                if (!player.getInventory().add(item)) player.drop(item, false);
            });
        });
    }

    public static void resetClass(Player player) {
        setClass(player, WANDERER, 0);
    }

    public static void removeClass(Player player) {
        setClass(player, NULL, 0);
    }

    public static boolean hasClass(Player player) {
        return player.getTags().stream().anyMatch(tag -> tag.startsWith("shadow_tags/class/"));
    }

    public static String getClassName(Player player) {
        return player.getTags().stream()
                .filter(tag -> tag.startsWith("shadow_tags/class/"))
                .findFirst()
                .map(tag -> tag.split("/")[2].toLowerCase())
                .orElse(NULL);
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