package com.lucab.shadows_things.exhaustion;

import com.lucab.shadows_things.ModGameRules;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ExhaustionHandler {
    private ExhaustionHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        Level level = player.level();
        if (isPeaceful(level) || !isExhaustionEnabled(level)) return;

        player.getFoodData().setExhaustion(0.0f);
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.isCreative() || player.isSpectator()) return;

        Level level = player.level();
        FoodData foodData = player.getFoodData();

        if (isPeaceful(level)) {
            if (foodData.getFoodLevel() < 20) {
                foodData.setFoodLevel(20);
            }
            foodData.setSaturation(0.0f);
            foodData.setExhaustion(-1.0f);
            return;
        }

        if (!isExhaustionEnabled(level)) return;

        int exhaustionDelay = ModGameRules.EXHAUSTION_DELAY.getValue(level);
        exhaustionDelay = Math.max(1, exhaustionDelay);
        ExhaustionData data = player.getData(ExhaustionData.EXHAUSTION);
        data.tick(player.getFoodData(), exhaustionDelay);
        player.setData(ExhaustionData.EXHAUSTION, data);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.hasEffect(MobEffects.REGENERATION)) return;

        if (isPeaceful(player.level()) || !isExhaustionEnabled(player.level())) return;

        boolean isNaturalRegen = StackWalker.getInstance()
                .walk(frames -> frames.anyMatch(frame ->
                        frame.getClassName().equals("net.minecraft.world.food.FoodData") && frame.getMethodName().equals("tick")
                ));

        if (isNaturalRegen) event.setCanceled(true);
    }

    private static boolean isPeaceful(Level level) {
        return level.getDifficulty() == Difficulty.PEACEFUL;
    }

    private static boolean isExhaustionEnabled(Level level) {
        return ModGameRules.EXHAUSTION_ENABLED.getValue(level);
    }
}
