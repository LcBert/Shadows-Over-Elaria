package com.lucab.shadows_things;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class ModGameRules {
    public record IntegerGameRule(GameRules.Key<GameRules.IntegerValue> gameRule) {
        public IntegerGameRule(String name, int value) {
            this(name, GameRules.Category.MISC, value);
        }

        public IntegerGameRule(String name, GameRules.Category category, int value) {
            this(GameRules.register(name, category, GameRules.IntegerValue.create(value)));
        }

        public int getValue(Level level) {
            return level.getGameRules().getInt(this.gameRule);
        }
    }

    public record BooleanGameRule(GameRules.Key<GameRules.BooleanValue> gameRule) {
        public BooleanGameRule(String name, boolean value) {
            this(name, GameRules.Category.MISC, value);
        }

        public BooleanGameRule(String name, GameRules.Category category, boolean value) {
            this(GameRules.register(name, category, GameRules.BooleanValue.create(value)));
        }

        public boolean getValue(Level level) {
            return level.getGameRules().getBoolean(this.gameRule);
        }
    }

    public static BooleanGameRule EXHAUSTION_ENABLED =
            new BooleanGameRule("exhaustionEnabled", true);

    public static IntegerGameRule EXHAUSTION_DELAY =
            new IntegerGameRule("exhaustionDelay", 6000);

    public static BooleanGameRule DO_CARCASS_SPAWN =
            new BooleanGameRule("doCarcassSpawn", true);

    public static void register() {
    }
}
