package com.lucab.shadows_things.exhaustion;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Supplier;

public class ExhaustionData implements INBTSerializable<CompoundTag> {
    public static final int EXHAUSTION_DELAY_INTERVAL = 200;

    private int exhaustionTick = EXHAUSTION_DELAY_INTERVAL;
    private int foodValue = 20;

    public int getExhaustionTick() {
        return exhaustionTick;
    }

    public void decrementTick() {
        this.exhaustionTick--;
    }

    public void resetTick() {
        this.exhaustionTick = EXHAUSTION_DELAY_INTERVAL;
    }

    public int getFoodValue() {
        return foodValue;
    }

    public void setFoodValue(int foodValue) {
        this.foodValue = foodValue;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("exhaustionTick", exhaustionTick);
        nbt.putInt("foodValue", foodValue);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        exhaustionTick = nbt.getInt("exhaustionTick");
        foodValue = nbt.getInt("foodValue");
    }

    public static final Supplier<AttachmentType<ExhaustionData>> EXHAUSTION = ShadowsThings.ATTACHMENT_TYPES
            .register("exhaustion", () -> AttachmentType.serializable(ExhaustionData::new).build());

    public static void register() {
    }
}
