package com.lucab.shadows_things.worldgen.features_type;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FeaturesRegistry {
    // Features Types
    public static final DeferredRegister<Feature<?>> FEATURES_TYPE =
            DeferredRegister.create(Registries.FEATURE, ShadowsThings.MODID);

    public static final DeferredHolder<Feature<?>, ClusterFeatureType> CLUSTER_FEATURE_TYPE =
            FEATURES_TYPE.register("cluster", () -> new ClusterFeatureType(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus modEventBus) {
        FEATURES_TYPE.register(modEventBus);
    }

}
