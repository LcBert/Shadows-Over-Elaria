package com.lucab.shadows_things;

import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> DO_CARCASS_SPAWN;

    public static void registerGameRules() {
        DO_CARCASS_SPAWN = GameRules.register(
                "doCarcassSpawn",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
    }
}
