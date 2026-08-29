package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class ClassManager {
    public static void syncClass(Player player) {
        player.setData(ClassPlayerData.CLASS_PLAYER_DATA.get(), getClassData(player));
    }

    public static ClassPlayerData getClassData(Player player) {
        return player.getData(ClassPlayerData.CLASS_PLAYER_DATA.get());
    }

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

        ClassPlayerData classPlayerData = getClassData(player);

        classPlayerData.setClassName(formattedClass);
        classPlayerData.setClassTier(tier);

        ClassModifierApplier.updatePlayerAttributes(player, rpgClass);

        equipClass(player);
        syncClass(player);
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
        return getClassData(player).getClassName();
    }

    public static int getTier(Player player) {
        return getClassData(player).getClassTier();
    }

    public static boolean is(Player player, String rpgClass) {
        return getClassName(player).equals(rpgClass);
    }

    public static void executeAction(Player player, int actionType) {
        if (actionType != 1 && actionType != 0) return;

        Level level = player.level();
        ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(getClassName(player));
        ClassPlayerData classPlayerData = ClassManager.getClassData(player);
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
            if (action.type.equals(ClassActions.ActionType.COMMAND.getType())) {
                if (player instanceof ServerPlayer serverPlayer) {
                    String commandToExecute = action.value;
                    MinecraftServer server = serverPlayer.getServer();
                    if (server != null) {
                        server.getCommands().performPrefixedCommand(
                                serverPlayer.createCommandSourceStack().withSuppressedOutput(),
                                commandToExecute
                        );
                    }
                }
            } else if (action.type.equals(ClassActions.ActionType.EFFECT.getType())) {
                if (action instanceof ClassActions.EffectActionData effectAction) {
                    Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(action.value)).orElse(null);
                    if (effectHolder == null) continue;
                    MobEffectInstance effectInstance = new MobEffectInstance(effectHolder, effectAction.duration, effectAction.amplifier);
                    player.addEffect(effectInstance);
                }
            }
        }
    }
}