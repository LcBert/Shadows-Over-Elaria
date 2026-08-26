package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ClassHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!ClassManager.hasClass(player)) {
            ClassManager.resetClass(player);
        } else {
            ClassManager.syncClass(player);
        }

        String playerClass = ClassManager.getClassName(player);
        ClassModifierApplier.updatePlayerAttributes(player, playerClass);
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.isCreative()) return;
        if (!ClassManager.hasClass(player)) return;

        Item heldItem = player.getMainHandItem().getItem();

        if (!ClassItemsManager.isCorrectItem(player, heldItem)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isCreative()) return;
        if (!ClassManager.hasClass(player)) return;

        Item usedItem = event.getItem().getItem();

        if (!ClassItemsManager.isCorrectItem(player, usedItem)) {
            event.setCanceled(true);
            event.setDuration(-1);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.isCreative()) return;

        EquipmentSlot slot = event.getSlot();
        if (!slot.isArmor()) return;

        ItemStack toStack = event.getTo();

        if (toStack.isEmpty()) return;

        if (!ClassItemsManager.isCorrectItem(player, toStack.getItem())) {
            if (!player.getInventory().add(toStack)) {
                player.drop(toStack, false);
            }

            player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}