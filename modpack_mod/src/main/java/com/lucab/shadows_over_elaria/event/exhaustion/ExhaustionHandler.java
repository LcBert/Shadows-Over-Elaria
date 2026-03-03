package com.lucab.shadows_over_elaria.event.exhaustion;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;
import com.lucab.shadows_over_elaria.attachments.ExhaustionAttachments;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ShadowsOverElaria.MODID)
public class ExhaustionHandler {
    private static final int FOOD_TICK = 20;
    private static boolean isActive;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || event.getEntity().isCreative())
            return;

        Player player = event.getEntity();
        FoodData playerFood = player.getFoodData();
        ExhaustionAttachments exhaustionData = player.getData(ExhaustionAttachments.EXHAUSTION.get());

        isActive = event.getEntity().level().getDifficulty() != Difficulty.PEACEFUL;
        event.getEntity().level().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(!isActive,
                event.getEntity().getServer());

        if (!isActive) {
            exhaustionData.setFoodValue(playerFood.getFoodLevel());
            exhaustionData.setExhaustionTick(FOOD_TICK);
            return;
        }

        playerFood.setSaturation(0);
        playerFood.setExhaustion(-1);
        exhaustionData.setFoodValue(playerFood.getFoodLevel());
        exhaustionData.decreaseExhaustionTick();

        if (exhaustionData.getExhaustionTick() <= 0 && playerFood.getFoodLevel() > 0) {
            exhaustionData.setExhaustionTick(FOOD_TICK);
            exhaustionData.decreaseFoodValue();
            playerFood.setFoodLevel(exhaustionData.getFoodValue());
        }
    }
}
