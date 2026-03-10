package com.lucab.shadows_things.attachments;

import java.util.function.Supplier;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ExhaustionAttachments implements INBTSerializable<CompoundTag> {
    public int exhaustion_tick = 0;
    public int food_value = 20;

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("ExhaustionTick", this.exhaustion_tick);
        nbt.putInt("FoodValue", this.food_value);
        return nbt;
    }

    public void setExhaustionTick(int tick) {
        this.exhaustion_tick = tick;
    }

    public void decreaseExhaustionTick() {
        this.exhaustion_tick -= 1;
    }

    public int getExhaustionTick() {
        return this.exhaustion_tick;
    }

    public void setFoodValue(int food_value) {
        this.food_value = food_value;
    }

    public void decreaseFoodValue() {
        this.food_value -= 1;
    }

    public int getFoodValue() {
        return this.food_value;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        this.exhaustion_tick = nbt.getInt("ExhaustionTick");
        this.food_value = nbt.getInt("FoodValue");
    }

    public static final Supplier<AttachmentType<ExhaustionAttachments>> EXHAUSTION = ShadowsThings.ATTACHMENT_TYPES
            .register(
                    "exhaustion",
                    () -> AttachmentType.serializable(ExhaustionAttachments::new).build());

    public static void register() {
    }
}
