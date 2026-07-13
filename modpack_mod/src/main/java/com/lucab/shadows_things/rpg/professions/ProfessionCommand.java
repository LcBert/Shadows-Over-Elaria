package com.lucab.shadows_things.rpg.professions;

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

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ProfessionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shadow")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("profession")
                                .then(Commands.argument("player", EntityArgument.player())

                                        // 1. GET Branch: Without arguments shows the full list, with an argument shows a specific profession level
                                        .then(Commands.literal("get")
                                                .executes(ProfessionCommand::getGeneralStatus)
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .executes(ProfessionCommand::getSpecificProfession)))

                                        // 2. SET Branch: Sets a profession level directly (Syntax: /shadow profession <player> set <profession> <value>)
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, ProfessionHelper.MAX_PROFESSION_LEVEL))
                                                                .executes(ProfessionCommand::setProfessionLevel))))

                                        // 3. RESET Branch: Without arguments resets all professions, with an argument resets only that specific profession
                                        .then(Commands.literal("reset")
                                                .executes(ProfessionCommand::resetAllProfessions)
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .executes(ProfessionCommand::resetSpecificProfession)))

                                        // Level Up
                                        .then(Commands.literal("levelUp")
                                                .executes(ProfessionCommand::levelUp))

                                        // 4. POINTS Branch: Handles lookups, setting, adding, and removing points using attachment methods
                                        .then(Commands.literal("points")
                                                .then(Commands.literal("get")
                                                        .executes(ProfessionCommand::getPointsLevel))
                                                .then(Commands.literal("getTotal")
                                                        .executes(ProfessionCommand::getTotalPointsLevel))
                                                .then(Commands.literal("set")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                                .executes(ctx -> handlePoints(ctx, PointOperation.SET))))
                                                .then(Commands.literal("add")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> handlePoints(ctx, PointOperation.ADD))))
                                                .then(Commands.literal("remove")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> handlePoints(ctx, PointOperation.REMOVE)))))


                                        // 5. EXPERIENCE Branch
                                        .then(Commands.literal("experience")
                                                .then(Commands.literal("get")
                                                        .executes(ProfessionCommand::getExperience))
                                                .then(Commands.literal("getTotalForLeveling")
                                                        .executes(ProfessionCommand::getTotalForLeveling))
                                                .then(Commands.literal("set")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                                .executes(ctx -> handleExperience(ctx, PointOperation.SET))))
                                                .then(Commands.literal("add")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> handleExperience(ctx, PointOperation.ADD))))
                                                .then(Commands.literal("remove")
                                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> handleExperience(ctx, PointOperation.REMOVE))))
                                        )
                                )
                        )
        );
    }

    // Autocomplete suggestions mapping directly from the Professions Enum constants
    private static CompletableFuture<Suggestions> suggestProfessions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            builder.suggest(profession.name().toLowerCase(Locale.ROOT));
        }
        return builder.buildFuture();
    }

    // Displays the global overview of all player professions and their current levels
    private static int getGeneralStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");

        StringBuilder text = new StringBuilder();
        text.append(String.format("§e=== %s's Professions ===§r\n", player.getName().getString()));
        text.append(String.format("§7Available Points: §6%d§r\n", ProfessionHelper.getPoints(player)));

        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            int level = ProfessionHelper.getLevel(player, profession);
            text.append(String.format("§7- §b%s§7 - §f%d§r\n", profession.name().toLowerCase(Locale.ROOT), level));
        }

        context.getSource().sendSuccess(() -> Component.literal(text.toString()), false);
        return 1;
    }

    // Displays the current tier level of a targeted, single profession
    private static int getSpecificProfession(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        String profInput = StringArgumentType.getString(context, "profession").toUpperCase(Locale.ROOT);

        try {
            ProfessionHelper.Professions profession = ProfessionHelper.Professions.valueOf(profInput);
            int level = ProfessionHelper.getLevel(player, profession);
            context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s's §b%s§7 level is: §f%d§r",
                    player.getName().getString(), profession.name().toLowerCase(Locale.ROOT), level)), false);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("§cInvalid profession name!"));
        }

        return 1;
    }

    // Hard-sets a specific level to a targeted profession entry
    private static int setProfessionLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        String profInput = StringArgumentType.getString(context, "profession").toUpperCase(Locale.ROOT);
        int value = IntegerArgumentType.getInteger(context, "value");

        try {
            ProfessionHelper.Professions profession = ProfessionHelper.Professions.valueOf(profInput);
            ProfessionHelper.setLevel(player, profession, value);
            context.getSource().sendSuccess(() -> Component.literal(String.format("§aSuccessfully set %s level to %d for %s§r",
                    profession.name().toLowerCase(Locale.ROOT), value, player.getName().getString())), false);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("§cInvalid profession name!"));
        }

        return 1;
    }

    // Resets all player professions to level 0 via ProfessionHelper
    private static int resetAllProfessions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.resetLevel(player);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§eAll professions have been reset to level 0 for %s§r",
                player.getName().getString())), false);
        return 1;
    }

    private static int levelUp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        boolean result = ProfessionHelper.tryLevelUp(player);

        if (result) {
            context.getSource().sendSuccess(() -> Component.literal(String.format(
                    "§aLevel up successful!§r\n§7%s has gained a new profession point.§r\n§7Available points: §6%d§r",
                    player.getName().getString(), ProfessionHelper.getPoints(player))), false);
        } else {
            int currentXp = ProfessionHelper.getExperience(player);
            int requiredXp = ProfessionHelper.getExperienceRequired(player);
            int diff = requiredXp - currentXp;

            context.getSource().sendFailure(Component.literal(String.format(
                    "§eLevel up failed: Insufficient XP.§r\n§7Progress: §f%d/%d §7XP. §c%d §7XP missing.§r",
                    currentXp, requiredXp, diff)));
        }

        return 1;
    }

    // Resets only the requested single profession to level 0 via ProfessionHelper
    private static int resetSpecificProfession(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        String profInput = StringArgumentType.getString(context, "profession").toUpperCase(Locale.ROOT);

        try {
            ProfessionHelper.Professions profession = ProfessionHelper.Professions.valueOf(profInput);
            ProfessionHelper.resetLevel(player, profession);
            context.getSource().sendSuccess(() -> Component.literal(String.format("§eSuccessfully reset §b%s§e level to 0 for %s§r",
                    profession.name().toLowerCase(Locale.ROOT), player.getName().getString())), false);
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("§cInvalid profession name!"));
        }

        return 1;
    }

    // Fetches and prints the current available job points from the player's attachment data
    private static int getPointsLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");

        context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s has §6%d§7 available profession points.§r",
                player.getName().getString(), ProfessionHelper.getPoints(player))), false);
        return 1;
    }

    private static int getTotalPointsLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");

        context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s has §6%d§7 total profession points.§r",
                player.getName().getString(), ProfessionHelper.getTotalPoints(player))), false);
        return 1;
    }

    // Direct interface handler managing points adjustments using encapsulation math from the attachment file
    private static int handlePoints(CommandContext<CommandSourceStack> context, PointOperation op) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        switch (op) {
            case SET -> ProfessionHelper.setPoints(player, amount);
            case ADD -> ProfessionHelper.addPoints(player, amount);
            case REMOVE -> ProfessionHelper.removePoints(player, amount);
        }

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aProfession points updated for %s. Current points balance: %d§r",
                player.getName().getString(), ProfessionHelper.getPoints(player))), false);
        return 1;
    }

    // Fetches and prints the current available job points from the player's attachment data
    private static int getExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");

        context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s has §6%d§7 profession experience.§r",
                player.getName().getString(), ProfessionHelper.getExperience(player))), false);
        return 1;
    }

    private static int getTotalForLeveling(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");

        context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s needs §6%d§7 profession experience for leveling, current: §6%d§r.§r",
                player.getName().getString(), ProfessionHelper.getExperienceRequired(player), ProfessionHelper.getExperience(player))), false);
        return 1;
    }

    // Direct interface handler managing points adjustments using encapsulation math from the attachment file
    private static int handleExperience(CommandContext<CommandSourceStack> context, PointOperation op) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        switch (op) {
            case SET -> ProfessionHelper.setExperience(player, amount);
            case ADD -> ProfessionHelper.addExperience(player, amount);
            case REMOVE -> ProfessionHelper.removeExperience(player, amount);
        }

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aProfession experience updated for %s. Current experience balance: %d§r",
                player.getName().getString(), ProfessionHelper.getExperience(player))), false);
        return 1;
    }

    private enum PointOperation {
        SET, ADD, REMOVE
    }
}