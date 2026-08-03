package com.lucab.shadows_things.rpg.gems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record GemAttribute(
        ResourceLocation attributeId,
        AttributeModifier.Operation operation,
        double baseValue
) {
    public double getValueForTier(int tier) {
        return baseValue * tier;
    }
}
