package com.lucab.shadows_things.rpg.classes;

import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClassActions {
    public enum ActionType {
        COMMAND("command", CommandActionData::parse),
        EFFECT("effect", EffectActionData::parse);

        private final String type;
        private final Function<JsonObject, ActionData> deserializer;
        private static final Map<String, ActionType> BY_ID = new HashMap<>();

        static {
            for (ActionType actionType : values()) {
                BY_ID.put(actionType.type, actionType);
            }
        }

        ActionType(String type, Function<JsonObject, ActionData> deserializer) {
            this.type = "shadows_things:" + type;
            this.deserializer = deserializer;
        }

        public String getType() {
            return type;
        }

        public static ActionData parse(JsonObject json) {
            if (!json.has("type")) return null;
            String typeId = json.get("type").getAsString();
            ActionType actionType = ActionType.BY_ID.get(typeId);
            return actionType != null ? actionType.deserializer.apply(json) : null;
        }
    }

    public static abstract class ActionData {
        public abstract ActionType getType();

        public abstract void execute(Player player);
    }

    public static class CommandActionData extends ActionData {
        public final String command;

        public CommandActionData(String command) {
            this.command = command;
        }

        public static CommandActionData parse(JsonObject json) {
            String command = json.get("command").getAsString();
            return new CommandActionData(command);
        }

        @Override
        public ActionType getType() {
            return ActionType.COMMAND;
        }

        @Override
        public void execute(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                MinecraftServer server = serverPlayer.getServer();
                if (server != null) {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack().withSuppressedOutput(),
                            this.command
                    );
                }
            }
        }
    }

    public static class EffectActionData extends ActionData {
        public final String effect;
        public final int duration;
        public final int amplifier;

        public EffectActionData(String effect, int duration, int amplifier) {
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public static EffectActionData parse(JsonObject json) {
            String effect = json.get("effect").getAsString();
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 200;
            int amplifier = json.has("amplifier") ? json.get("amplifier").getAsInt() : 0;
            return new EffectActionData(effect, duration, amplifier);
        }

        @Override
        public ActionType getType() {
            return ActionType.EFFECT;
        }

        @Override
        public void execute(Player player) {
            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect)).orElse(null);
            if (effectHolder != null) {
                MobEffectInstance effectInstance = new MobEffectInstance(effectHolder, duration, amplifier);
                player.addEffect(effectInstance);
            }
        }
    }
}
