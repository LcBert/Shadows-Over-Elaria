package com.lucab.shadows_things.exhaustion;

import com.lucab.shadows_things.ShadowsThings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public class ExhaustionData {
    public static final float VISUAL_MAX_EXHAUSTION = 3.99f;

    public static final Codec<ExhaustionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("exhaustion_tick").forGetter(ExhaustionData::getExhaustionTick)
            ).apply(instance, ExhaustionData::new)
    );

    private int exhaustionTick;

    public ExhaustionData() {
        this(0);
    }

    public ExhaustionData(int exhaustionTick) {
        this.exhaustionTick = Math.max(0, exhaustionTick);
    }

    public int getExhaustionTick() {
        return exhaustionTick;
    }

    public void resetTick() {
        this.exhaustionTick = 0;
    }

    public float calculateVisualExhaustion(int exhaustionDelay) {
        float progress = (float) this.exhaustionTick / exhaustionDelay;
        return Mth.clamp(progress * VISUAL_MAX_EXHAUSTION, 0.0f, VISUAL_MAX_EXHAUSTION);
    }

    public void tick(FoodData foodData, int exhaustionDelay) {
        if (foodData.getSaturationLevel() > 0.0f) foodData.setSaturation(0.0f);

        this.exhaustionTick++;

        if (this.exhaustionTick > exhaustionDelay) {
            resetTick();
            if (foodData.getFoodLevel() > 0) {
                foodData.setFoodLevel(foodData.getFoodLevel() - 1);
            }
        }

        foodData.setExhaustion(calculateVisualExhaustion(exhaustionDelay));
    }

    public static final Supplier<AttachmentType<ExhaustionData>> EXHAUSTION = ShadowsThings.ATTACHMENT_TYPES.register(
            "exhaustion",
            () -> AttachmentType.builder(() -> new ExhaustionData())
                    .serialize(ExhaustionData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static void register() {
    }
}
