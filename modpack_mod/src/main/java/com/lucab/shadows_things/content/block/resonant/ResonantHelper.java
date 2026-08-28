package com.lucab.shadows_things.content.block.resonant;

import net.minecraft.core.BlockPos;

public class ResonantHelper {
    public static final BlockPos[] PEDESTAL_OFFSETS = new BlockPos[]{
            new BlockPos(0, 0, -3), // North
            new BlockPos(0, 0, 3), // South
            new BlockPos(-3, 0, 0), // West
            new BlockPos(3, 0, 0), // East
            new BlockPos(-2, 0, -2), // North-West
            new BlockPos(2, 0, -2), // North-East
            new BlockPos(-2, 0, 2), // South-West
            new BlockPos(2, 0, 2)  // South-East
    };
}
