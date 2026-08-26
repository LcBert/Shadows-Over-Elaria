package com.lucab.shadows_things.exhaustion;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.DifficultyChangeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ExhaustionHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(0);
            player.getFoodData().setExhaustion(-1);
            return;
        }

        FoodData foodData = player.getFoodData();
        ExhaustionData exhaustionData = player.getData(ExhaustionData.EXHAUSTION);

        // Remove saturation
        if (foodData.getSaturationLevel() > 0) foodData.setSaturation(0);

        // Set exhaustion progress
        float exhaustionProgress = (1 - ((float) exhaustionData.getExhaustionTick() / ExhaustionData.EXHAUSTION_DELAY_INTERVAL)) * 4.0f;
        foodData.setExhaustion(exhaustionProgress);

        if (foodData.getFoodLevel() != exhaustionData.getFoodValue()) {
            exhaustionData.setFoodValue(foodData.getFoodLevel());
        }

        exhaustionData.decrementTick();

        if (exhaustionData.getExhaustionTick() <= 0) {
            exhaustionData.resetTick();
            if (foodData.getFoodLevel() > 0) {
                foodData.setFoodLevel(foodData.getFoodLevel() - 1);
                exhaustionData.setFoodValue(foodData.getFoodLevel());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                applyRegenRule(server, level.getDifficulty());
            }
        }
    }

    @SubscribeEvent
    public static void onDifficultyChange(DifficultyChangeEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            applyRegenRule(server, event.getDifficulty());
        }
    }

    private static void applyRegenRule(MinecraftServer server, Difficulty difficulty) {
        boolean isPeaceful = (difficulty == Difficulty.PEACEFUL);
        server.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(isPeaceful, server);
    }
}
