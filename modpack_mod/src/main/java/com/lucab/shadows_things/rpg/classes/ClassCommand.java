package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
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
                                                                .executes(ClassCommand::setClassAndTier))
                                                        .executes(ClassCommand::setClass)))
                                        .then(Commands.literal("setTier")
                                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 5))
                                                        .suggests(ClassCommand::suggestTiers)
                                                        .executes(ClassCommand::setTier)))
                                        .then(Commands.literal("get")
                                                .executes(ClassCommand::getClass))
                                        .then(Commands.literal("reset")
                                                .executes(ClassCommand::resetClass))
                                        .then(Commands.literal("remove")
                                                .executes(ClassCommand::removeClass))
                                        .then(Commands.literal("experience")
                                                .then(Commands.literal("set")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                                .executes(ClassCommand::setExperience)))
                                                .then(Commands.literal("add")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                                                .executes(ClassCommand::addExperience)))
                                                .then(Commands.literal("remove")
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                                                .executes(ClassCommand::removeExperience)))
                                                .then(Commands.literal("reset")
                                                        .executes(ClassCommand::resetExperience))
                                                .then(Commands.literal("get")
                                                        .executes(ClassCommand::getExperience))
                                        )
                                )));
    }

    private static CompletableFuture<Suggestions> suggestClasses(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        // Dynamically suggests names registered via JSON file
        for (String className : ShadowsThings.CLASS_READER.getAllClasses().keySet()) {
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

    private static int setClassAndTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        String className = StringArgumentType.getString(context, "class");
        int tier = IntegerArgumentType.getInteger(context, "tier");

        try {
            ClassManager.setClass(player, className, tier);
            // SUCCESS: Green message, styled layout for class and tier updates
            source.sendSuccess(() -> Component.literal(String.format("§aSuccessfully set %s's class to §b%s§a (Tier §6%d§a)§r",
                    player.getScoreboardName(), ClassManager.getClassName(player).toUpperCase(), tier)), false);
        } catch (IllegalArgumentException e) {
            // FAILURE: Standard red error notification
            source.sendSuccess(() -> Component.literal(String.format("§cInvalid class text or tier: %s§r", className)), false);
        }

        return 1;
    }

    private static int setClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        String className = StringArgumentType.getString(context, "class");

        try {
            ClassManager.setClass(player, className, 1);
            // SUCCESS: Green message, styled layout for class and tier updates
            source.sendSuccess(() -> Component.literal(String.format("§aSuccessfully set %s's class to §b%s§a (Tier §6%d§a)§r",
                    player.getScoreboardName(), ClassManager.getClassName(player).toUpperCase(), 1)), false);
        } catch (IllegalArgumentException e) {
            // FAILURE: Standard red error notification
            source.sendSuccess(() -> Component.literal(String.format("§cInvalid class text or tier: %s§r", className)), false);
        }

        return 1;
    }

    private static int setTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        int tier = IntegerArgumentType.getInteger(context, "tier");

        try {
            ClassManager.setTier(player, tier);
            source.sendSuccess(() -> Component.literal(String.format("§aSuccessfully set %s's tier to §b%s§r", player.getScoreboardName(), tier)),
                    false);
        } catch (IllegalArgumentException e) {
            source.sendSuccess(() -> Component.literal(String.format("§cInvalid class tier: %d§r", tier)), false);
        }

        return 1;
    }

    private static int getClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        if (!ClassManager.hasClass(player)) {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7%s currently has §cno class§7.§r",
                    player.getScoreboardName())), false);
        } else if (ClassManager.getClassName(player).equals(ClassPlayerData.WANDERER_CLASS)) {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7Player: §f%s§r\n§7Class:  §b%s§r",
                    player.getScoreboardName(), ClassManager.getClassName(player).toUpperCase())), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("§e=== RPG Class Status ===§r\n§7Player: §f%s§r\n§7Class:  §b%s§r\n§7Tier:   §6%d§r",
                    player.getScoreboardName(), ClassManager.getClassName(player).toUpperCase(), ClassManager.getTier(player))), false);
        }
        return 1;
    }

    private static int resetClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.resetClass(player);
        source.sendSuccess(() -> Component.literal(String.format("§eReset %s's class back to §b%s§r",
                player.getScoreboardName(), ClassPlayerData.WANDERER_CLASS.toUpperCase())), false);
        return 1;
    }

    private static int removeClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.removeClass(player);
        source.sendSuccess(() -> Component.literal(String.format("§eSuccessfully removed active RPG class from %s§r",
                player.getScoreboardName())), false);

        return 1;
    }

    private static int setExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        int value = IntegerArgumentType.getInteger(context, "value");

        ClassManager.setExperience(player, value);

        int currentExp = ClassManager.getExperience(player);
        int maxExp = ClassManager.getExperienceRequired(player);

        source.sendSuccess(() -> Component.literal("Set ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("'s experience to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(currentExp)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("/" + maxExp).withStyle(ChatFormatting.DARK_GREEN)), true);

        return 1;
    }

    private static int addExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        int value = IntegerArgumentType.getInteger(context, "value");

        ClassManager.addExperience(player, value);

        int currentExp = ClassManager.getExperience(player);
        int maxExp = ClassManager.getExperienceRequired(player);

        source.sendSuccess(() -> Component.literal("Added ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" experience to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format(" (%d/%d)", currentExp, maxExp)).withStyle(ChatFormatting.DARK_GREEN)), true);

        return currentExp;
    }

    private static int removeExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");
        int value = IntegerArgumentType.getInteger(context, "value");

        ClassManager.removeExperience(player, value);

        int currentExp = ClassManager.getExperience(player);
        int maxExp = ClassManager.getExperienceRequired(player);

        source.sendSuccess(() -> Component.literal("Removed ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" experience from ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.format(" (%d/%d)", currentExp, maxExp)).withStyle(ChatFormatting.DARK_GREEN)), true);

        return currentExp;
    }

    private static int resetExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        ClassManager.resetExperience(player);

        source.sendSuccess(() -> Component.literal("Reset experience for ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" to 0.").withStyle(ChatFormatting.YELLOW)), true);

        return 0;
    }

    private static int getExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = EntityArgument.getPlayer(context, "player");

        int currentExp = ClassManager.getExperience(player);
        int maxExp = ClassManager.getExperienceRequired(player);
        float progress = ClassManager.getExperienceProgress(player) * 100.0f;

        source.sendSuccess(() -> Component.literal("=== RPG Experience Status ===")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\nPlayer: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nProgress: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("%d / %d (%.1f%%)", currentExp, maxExp, progress)).withStyle(ChatFormatting.AQUA)), false);

        return currentExp;
    }
}