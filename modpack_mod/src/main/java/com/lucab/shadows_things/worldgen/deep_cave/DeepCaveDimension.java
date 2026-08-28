package com.lucab.shadows_things.worldgen.deep_cave;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public class DeepCaveDimension {
    public static final ResourceKey<DimensionType> DEEP_CAVE_TYPE_KEY = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave")
    );

    public static final ResourceKey<Level> DEEP_CAVE_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave")
    );

    public static final ResourceKey<LevelStem> DEEP_CAVE_STEM_KEY = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave")
    );

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(DEEP_CAVE_TYPE_KEY, new DimensionType(
                OptionalLong.of(6000),   // fixedTime
                false, // hasSkyLight
                true, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0D, // coordinateScale
                false, // bedWorks
                false, // respawnAnchorWorks
                DeepCaveData.minY, // minY
                DeepCaveData.getHeight(), // height
                DeepCaveData.getHeight(), // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave_effects"), // effectsLocation
                0.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0) // monsterSettings
        ));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        Holder<DimensionType> dimTypeHolder = context.lookup(Registries.DIMENSION_TYPE).getOrThrow(DEEP_CAVE_TYPE_KEY);
        Holder<NoiseGeneratorSettings> noiseSettingsHolder = context.lookup(Registries.NOISE_SETTINGS).getOrThrow(DeepCaveNoiseSettings.DEEP_CAVE_NOISE_SETTINGS);

        ResourceKey<Biome> deepCaveBiomeKey = ResourceKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave")
        );

        NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(context.lookup(Registries.BIOME).getOrThrow(deepCaveBiomeKey)),
                noiseSettingsHolder
        );

        LevelStem stem = new LevelStem(dimTypeHolder, noiseBasedChunkGenerator);
        context.register(DEEP_CAVE_STEM_KEY, stem);
    }
}
