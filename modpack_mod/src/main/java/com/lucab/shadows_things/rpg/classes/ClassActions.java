package com.lucab.shadows_things.rpg.classes;

public class ClassActions {
    public enum ActionType {
        COMMAND("command"),
        EFFECT("effect");

        private final String type;

        ActionType(String type) {
            this.type = "shadows_things:" + type;
        }

        public String getType() {
            return type;
        }
    }

    public static class ActionData {
        public final String type;
        public final String value;

        public ActionData(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    public static class CommandActionData extends ActionData {
        public CommandActionData(String type, String value) {
            super(type, value);
        }
    }

    public static class EffectActionData extends ActionData {
        public final int duration;
        public final int amplifier;

        public EffectActionData(String type, String value, int duration, int amplifier) {
            super(type, value);
            this.duration = duration;
            this.amplifier = amplifier;
        }
    }
}
