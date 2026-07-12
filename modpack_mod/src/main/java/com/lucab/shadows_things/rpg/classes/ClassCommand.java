package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
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
        // Dynamically suggests names registered via JSON file
        for (String className : ShadowsThings.RPG_READER.getAllClasses().keySet()) {
            builder.suggest(className);
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
            // SUCCESS: Green message, styled layout for class and tier updates
            source.sendSuccess(() -> Component.literal(String.format("§aSuccessfully set %s's class to §b%s§a (Tier §6%d§a)§r",
                    player.getName().getString(), ClassManager.getClassName(player).toUpperCase(), tier)), false);
        } catch (IllegalArgumentException e) {
            // FAILURE: Standard red error notification
            source.sendSuccess(() -> Component.literal(String.format("§cInvalid class name or tier: %s§r", className)), false);
        }

        return 1;
    }

    private static int getClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        // GET Responses: Beautiful formatted layouts using consistent colors
        if (!ClassManager.hasClass(player)) {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7%s currently has §cno class§7.§r",
                    player.getName().getString())), false);
        } else if (ClassManager.getClassName(player).equals(ClassManager.WANDERER)) {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7Player: §f%s§r\n§7Class:  §b%s§r",
                    player.getName().getString(), ClassManager.getClassName(player).toUpperCase())), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7Player: §f%s§r\n§7Class:  §b%s§r\n§7Tier:   §6%d§r",
                    player.getName().getString(), ClassManager.getClassName(player).toUpperCase(), ClassManager.getTier(player))), false);
        }
        return 1;
    }

    private static int resetClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.resetClass(player);
        // RESET: Gold warning / feedback color to indicate status reset to default
        source.sendSuccess(() -> Component.literal(String.format("§eReset %s's class back to §b%s§r",
                player.getName().getString(), ClassManager.WANDERER.toUpperCase())), false);
        return 1;
    }

    private static int removeClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.removeClass(player);
        // REMOVE: Distinct format indicating permanent removal of the active role
        source.sendSuccess(() -> Component.literal(String.format("§eSuccessfully removed all active RPG classes from %s§r",
                player.getName().getString())), false);

        return 1;
    }
}