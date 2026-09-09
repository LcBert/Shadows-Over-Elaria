package com.lucab.shadows_things;

import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> EXHAUSTION_ENABLED;
    public static GameRules.Key<GameRules.IntegerValue> EXHAUSTION_DELAY;

    public static GameRules.Key<GameRules.BooleanValue> DO_CARCASS_SPAWN;

    public static void registerGameRules() {
        EXHAUSTION_ENABLED = GameRules.register(
                "exhaustionEnabled",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );

        EXHAUSTION_DELAY = GameRules.register(
                "exhaustionDelay",
                GameRules.Category.MISC,
                GameRules.IntegerValue.create(6000)
        );

        DO_CARCASS_SPAWN = GameRules.register(
                "doCarcassSpawn",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
    }
}
