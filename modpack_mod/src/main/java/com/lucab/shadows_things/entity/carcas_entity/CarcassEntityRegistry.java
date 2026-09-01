package com.lucab.shadows_things.entity.carcas_entity;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CarcassEntityRegistry {
    public static final DeferredHolder<EntityType<?>, EntityType<CarcassEntity>> CARCASS_ENTITY = ShadowsThings.ENTITIES.register(
            "carcass_entity", () -> EntityType.Builder.<CarcassEntity>of(CarcassEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("carcass_entity")
    );

    public static void register() {
    }
}
