package com.lucab.shadows_things.worldgen.deep_cave;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeepCaveNoiseSettings {
    private static final int MIN_Y = 0;
    private static final int HEIGHT = 512;
    private static final int LAYERS_GAP = 5;
    private static final List<Block> LAYERS = List.of(
            Blocks.STONE,
            Blocks.TUFF,
            Blocks.DEEPSLATE,
            Blocks.BASALT,
            Blocks.BLACKSTONE
    );

    public static final ResourceKey<NoiseGeneratorSettings> DEEP_CAVE_NOISE_SETTINGS =
            ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave_noise_settings"));

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(DEEP_CAVE_NOISE_SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(MIN_Y, HEIGHT, 1, 1), //noiseSettings
                Blocks.STONE.defaultBlockState(), // defaultBlock
                Blocks.WATER.defaultBlockState(), // defaultFluid
                createCustomNoiseRouter(), // noiseRouter
                createCustomSurfaceRule(), // surfaceRule
                List.of(), // spawnTarget
                -64, // Sea Level
                true,  // disable_mob_generation
                true,  // aquifers_enabled
                true,  // ore_veins_enabled
                false  // legacy_random_source
        ));
    }

    private static NoiseRouter createCustomNoiseRouter() {
        return new NoiseRouter(
                DensityFunctions.zero(), // barrierNoise
                DensityFunctions.zero(), // fluidLevelFloodednessNoise
                DensityFunctions.zero(), // fluidLevelSpreadNoise
                DensityFunctions.zero(), // lavaNoise
                DensityFunctions.zero(), // temperature
                DensityFunctions.zero(), // vegetation
                DensityFunctions.zero(), // continents
                DensityFunctions.zero(), // erosion
                DensityFunctions.zero(), // depth
                DensityFunctions.zero(), // ridges
                DensityFunctions.zero(), // initialDensityWithoutJaggedness
                DensityFunctions.constant(0.1), // finalDensity
                DensityFunctions.zero(), // veinToggle
                DensityFunctions.zero(), // veinRidged
                DensityFunctions.zero()  // veinGap
        );
    }

    private static SurfaceRules.RuleSource createCustomSurfaceRule() {
        SurfaceRules.RuleSource bedrockFloor = SurfaceRules.ifTrue(
                SurfaceRules.verticalGradient(ShadowsThings.MODID + ":bedrock_floor", VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(1)),
                SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())
        );

        SurfaceRules.RuleSource bedrockRoof = SurfaceRules.ifTrue(
                SurfaceRules.not(
                        SurfaceRules.verticalGradient(ShadowsThings.MODID + ":bedrock_roof", VerticalAnchor.belowTop(1), VerticalAnchor.belowTop(0))
                ),
                SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())
        );

        List<SurfaceRules.RuleSource> layersRules = new ArrayList<>();
        for (int i = 0; i < DeepCaveData.layers.size(); i++) {
            DeepCaveData.Layer layer = DeepCaveData.layers.get(i);
            int[] coords = DeepCaveData.getLayerCoordinates(i + 1);

            SurfaceRules.RuleSource rule = SurfaceRules.ifTrue(
                    SurfaceRules.verticalGradient(
                            ShadowsThings.MODID + ":" + layer.randomName(),
                            VerticalAnchor.absolute(coords[1]),
                            VerticalAnchor.absolute(coords[0])
                    ),
                    SurfaceRules.state(layer.block().defaultBlockState())
            );
            layersRules.add(rule);
        }
        Collections.reverse(layersRules);

        List<SurfaceRules.RuleSource> allRules = new ArrayList<>();
        allRules.add(bedrockFloor);
        allRules.addAll(layersRules);
        allRules.add(bedrockRoof);

        return SurfaceRules.sequence(allRules.toArray(new SurfaceRules.RuleSource[0]));
    }
}
