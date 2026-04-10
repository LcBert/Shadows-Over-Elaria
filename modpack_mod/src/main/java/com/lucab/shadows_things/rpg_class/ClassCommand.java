package com.lucab.shadows_things.rpg_class;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;

public class ClassCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shadow")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("class")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("class", StringArgumentType.word())
                                                        .suggests(ClassCommand::suggestClasses)
                                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                                .suggests(ClassCommand::suggestTiers)
                                                                .executes(ClassCommand::setClass))))
                                        .then(Commands.literal("get")
                                                .executes(ClassCommand::getClass))
                                        .then(Commands.literal("reset")
                                                .executes(ClassCommand::resetClass))
                                        .then(Commands.literal("remove")
                                                .executes(ClassCommand::removeClass))
                                )));
    }

    private static CompletableFuture<Suggestions> suggestClasses(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (ClassManager.RPGClass rpgClass : ClassManager.RPGClass.values()) {
            if (rpgClass != ClassManager.RPGClass.WANDERER)
                builder.suggest(rpgClass.name().toLowerCase());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestTiers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (int i = 1; i <= 5; i++) {
            builder.suggest(i);
        }
        return builder.buildFuture();
    }

    private static int setClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        String className = StringArgumentType.getString(context, "class");
        int tier = IntegerArgumentType.getInteger(context, "tier");

        try {
            ClassManager.setClass(player, className, tier);
            source.sendSuccess(() -> Component.literal(String.format("Set %s class to %s (Tier %d)", player.getName().getString(), ClassManager.getClassName(player), tier)), false);
        } catch (IllegalArgumentException e) {
            source.sendSuccess(() -> Component.literal("Invalid class name: " + className).withColor(0xFF0000), false);
        }

        return 1;
    }

    private static int getClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        source.sendSuccess(() -> Component.literal(String.format("%s's class: %s (Tier %d)",
                        player.getName().getString(),
                        ClassManager.getClassName(player),
                        ClassManager.getTier(player))),
                false);
        return 1;
    }

    private static int resetClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.resetClass(player);
        source.sendSuccess(() -> Component.literal(String.format("Reset %s's class to %s", player.getName().getString(), ClassManager.RPGClass.WANDERER.name())), false);
        return 1;
    }

    private static int removeClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.removeClass(player);
        source.sendSuccess(() -> Component.literal(String.format("Removed %s's class", player.getName().getString())), false);

        return 1;
    }
}
