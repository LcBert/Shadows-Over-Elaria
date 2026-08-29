package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class ClassModifierApplier {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "class_modifier");

    public static void updatePlayerAttributes(Player player, String className) {
        if (player.level().isClientSide()) return;

        float healthRatio = player.getHealth() / player.getMaxHealth();

        BuiltInRegistries.ATTRIBUTE.holders().forEach(attributeHolder -> {
            AttributeInstance instance = player.getAttribute(attributeHolder);
            if (instance != null) instance.removeModifier(MODIFIER_ID);
        });

        if (className.equals(ClassPlayerData.WANDERER_CLASS) || className.equals(ClassPlayerData.NONE_CLASS)) {
            restoreProportionalHealth(player, healthRatio);
            return;
        }

        ClassDataReader.ClassData classData = ShadowsThings.CLASS_READER.getClassData(className);

        if (classData != null) {
            for (ClassDataReader.ClassAttribute parsedAttr : classData.attributes()) {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(parsedAttr.attributeId());
                if (attribute != null) {
                    AttributeInstance instance = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
                    if (instance != null) {

                        AttributeModifier modifier = new AttributeModifier(
                                MODIFIER_ID,
                                parsedAttr.value(),
                                parsedAttr.operation()
                        );
                        instance.addPermanentModifier(modifier);
                    }
                }
            }
        }
        ;

        restoreProportionalHealth(player, healthRatio);
    }

    private static void restoreProportionalHealth(Player player, float oldHealthRatio) {
        float newHealth = player.getMaxHealth() * oldHealthRatio;

        if (newHealth < 1.0F && oldHealthRatio > 0.0F) {
            newHealth = 1.0F;
        }

        player.setHealth(newHealth);
    }
}
