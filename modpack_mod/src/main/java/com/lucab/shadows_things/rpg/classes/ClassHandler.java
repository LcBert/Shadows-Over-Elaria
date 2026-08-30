package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ClassHandler {
    /**
     * <h1>Handle class on player login</h1>
     * <p>Reset to WANDERER if player has no class and sync to client</p>
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;
        Player player = event.getEntity();
        if (!ClassManager.hasClass(player)) ClassManager.resetClass(player);
        ClassPlayerData.sync(player);
        ClassModifierApplier.updatePlayerAttributes(player, ClassManager.getClassName(player));
    }

    /**
     * <h1>Handle player kill an entity</h1>
     * <p>Give experience based on entity max health and damage contribution</p>
     */
    @SubscribeEvent
    public static void onPlayerKillEntity(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (attacker.level().isClientSide) return;
        if (attacker.getAbilities().instabuild) return;
        if (!ClassManager.hasClass(attacker)) return;

        MinecraftServer server = attacker.level().getServer();
        if (server == null) return;

        ClassEntityData classEntityData = monster.getData(ClassEntityData.CLASS_ENTITY_DATA);
        float entityHealth = event.getEntity().getMaxHealth();

        classEntityData.getPlayersDamage().forEach((uuid, damage) -> {
            Player player = server.getPlayerList().getPlayer(uuid);
            if (player == null) return;

            float experienceGained = ClassManager.getKillFormula(entityHealth, damage);
            int expToInt = Math.round(experienceGained);
            if (expToInt > 0) {
                ClassManager.addExperience(player, expToInt);
                ClassManager.levelUp(player);
            }
        });
    }

    /**
     * <h1>Handle living take damage</h1>
     * <p>Cancel event if player is attacking entity with a weapons of wrong class or tier</p>
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (player.level().isClientSide) return;
        if (player.getAbilities().instabuild) return;
        if (!ClassManager.hasClass(player)) return;

        Item heldItem = player.getMainHandItem().getItem();

        if (!ClassItemsManager.isCorrectItem(player, heldItem)) {
            event.setCanceled(true);
        } else {
            ClassEntityData classEntityData = monster.getData(ClassEntityData.CLASS_ENTITY_DATA);
            classEntityData.addDamage(player.getUUID(), event.getAmount());
        }
    }

    /**
     * <h1>Handle player use items</h1>
     * <p>Cancel event if player is using a weapons of wrong class or tier</p>
     */
    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getAbilities().instabuild) return;
        if (!ClassManager.hasClass(player)) return;

        Item usedItem = event.getItem().getItem();

        if (!ClassItemsManager.isCorrectItem(player, usedItem)) {
            event.setCanceled(true);
            event.setDuration(-1);
        }
    }

    /**
     * <h1>Handle player equip armor</h1>
     * <p>Cancel event if player is equipping an armor of wrong class or tier</p>
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player) || player.getAbilities().instabuild) return;

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