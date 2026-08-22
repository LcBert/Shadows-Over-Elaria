package com.lucab.shadows_things.worldgen.DeepCave;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

public class DeepCaveData {
    public record Layer(Block block, String randomName) {
    }

    public static final int minY = 0;
    public static final int maxY = 511;

    public static final List<Layer> layers = List.of(
            new Layer(Blocks.STONE, "layer_i"),
            new Layer(Blocks.TUFF, "layer_ii"),
            new Layer(Blocks.DEEPSLATE, "layer_iii"),
            new Layer(Blocks.BASALT, "layer_iv"),
            new Layer(Blocks.BLACKSTONE, "layer_v")
    );
    public static final int layersSeparations = 5;

    public static int getHeight() {
        return maxY - minY + 1;
    }

    public static int[] getLayerCoordinates(int layerIndex) {
        int totalLayers = layers.size();
        if (layerIndex < 1 || layerIndex > totalLayers) return new int[0];

        int totalHeight = getHeight();
        int sectionHeight = totalHeight / totalLayers;

        int invertedIndex = totalLayers - layerIndex;

        int layerMinY = minY + (invertedIndex * sectionHeight) + sectionHeight;
        int layerMaxY = layerMinY - layersSeparations;

        return new int[]{layerMinY, layerMaxY};
    }
}
