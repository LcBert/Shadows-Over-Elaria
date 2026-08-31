package com.lucab.shadows_things.rpg.professions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ProfessionCommand {

    private static final DynamicCommandExceptionType ERROR_INVALID_PROFESSION = new DynamicCommandExceptionType(
            name -> Component.literal(String.format("Invalid profession '%s'!", name))
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shadow")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("profession")
                                .then(Commands.argument("player", EntityArgument.player())

                                        // 1. GET: Query all professions or inspect a specific one
                                        .then(Commands.literal("get")
                                                .executes(ProfessionCommand::getGeneralStatus)
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .executes(ProfessionCommand::getSpecificProfession)))

                                        // 2. SET: Hard-set level or experience for a target profession
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .then(Commands.literal("level")
                                                                .then(Commands.argument("level", IntegerArgumentType.integer(0, ProfessionHelper.MAX_PROFESSION_LEVEL))
                                                                        .executes(ctx -> setLevel(ctx, IntegerArgumentType.getInteger(ctx, "level")))))
                                                        .then(Commands.literal("experience")
                                                                .then(Commands.argument("experience", IntegerArgumentType.integer(0))
                                                                        .executes(ctx -> setExperience(ctx, IntegerArgumentType.getInteger(ctx, "experience")))))))

                                        // 3. ADD: Incremental modifiers for level and experience
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .then(Commands.literal("level")
                                                                .then(Commands.argument("levels", IntegerArgumentType.integer(1, ProfessionHelper.MAX_PROFESSION_LEVEL))
                                                                        .executes(ctx -> addLevel(ctx, IntegerArgumentType.getInteger(ctx, "levels")))))
                                                        .then(Commands.literal("experience")
                                                                .then(Commands.argument("experience", IntegerArgumentType.integer(1))
                                                                        .executes(ctx -> addExperience(ctx, IntegerArgumentType.getInteger(ctx, "experience")))))))

                                        // 4. RESET: Reset single profession or wipe all back to level 0
                                        .then(Commands.literal("reset")
                                                .executes(ProfessionCommand::resetAll)
                                                .then(Commands.argument("profession", StringArgumentType.word())
                                                        .suggests(ProfessionCommand::suggestProfessions)
                                                        .executes(ProfessionCommand::resetSpecificProfession)))
                                )
                        )
        );
    }

    // =========================================================================
    // AUTOCOMPLETION & PARSING
    // =========================================================================

    private static CompletableFuture<Suggestions> suggestProfessions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(ProfessionHelper.Professions.values())
                        .map(prof -> prof.name().toLowerCase(Locale.ROOT)),
                builder
        );
    }

    private static ProfessionHelper.Professions parseProfession(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, argumentName);
        try {
            return ProfessionHelper.Professions.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_PROFESSION.create(input);
        }
    }

    // =========================================================================
    // GET ACTIONS
    // =========================================================================

    private static int getGeneralStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        StringBuilder output = new StringBuilder();
        output.append(String.format("§e=== %s's Professions ===§r\n", player.getName().getString()));

        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            int level = ProfessionHelper.getLevel(player, profession);
            int exp = ProfessionHelper.getExperience(player, profession);
            int reqExp = ProfessionHelper.getRequiredExperience(player, profession);

            if (ProfessionHelper.isMaxLevel(player, profession)) {
                output.append(String.format("§7- §b%s§7: Level §a%d §6(MAX)§r\n",
                        profession.name().toLowerCase(Locale.ROOT), level));
            } else {
                output.append(String.format("§7- §b%s§7: Level §a%d §7[§e%d§7/§e%d XP§7]§r\n",
                        profession.name().toLowerCase(Locale.ROOT), level, exp, reqExp));
            }
        }

        context.getSource().sendSuccess(() -> Component.literal(output.toString()), false);
        return 1;
    }

    private static int getSpecificProfession(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        int level = ProfessionHelper.getLevel(player, profession);
        int exp = ProfessionHelper.getExperience(player, profession);
        int reqExp = ProfessionHelper.getRequiredExperience(player, profession);

        if (ProfessionHelper.isMaxLevel(player, profession)) {
            context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s's §b%s§7 level: §a%d §6(MAX)§r",
                    player.getName().getString(), profession.name().toLowerCase(Locale.ROOT), level)), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal(String.format("§7%s's §b%s§7 level: §a%d §7[§e%d§7/§e%d XP§7]§r",
                    player.getName().getString(), profession.name().toLowerCase(Locale.ROOT), level, exp, reqExp)), false);
        }

        return 1;
    }

    // =========================================================================
    // SET ACTIONS
    // =========================================================================

    private static int setLevel(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        ProfessionHelper.setLevel(player, profession, level);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aSet %s level to %d for %s.§r",
                profession.name().toLowerCase(Locale.ROOT), level, player.getName().getString())), true);
        return 1;
    }

    private static int setExperience(CommandContext<CommandSourceStack> context, int exp) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        ProfessionHelper.setExperience(player, profession, exp);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aSet %s experience to %d for %s.§r",
                profession.name().toLowerCase(Locale.ROOT), exp, player.getName().getString())), true);
        return 1;
    }

    // =========================================================================
    // ADD ACTIONS
    // =========================================================================

    private static int addLevel(CommandContext<CommandSourceStack> context, int levels) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        ProfessionHelper.addLevel(player, profession, levels);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aAdded %d level(s) to %s for %s. Current level: %d§r",
                levels, profession.name().toLowerCase(Locale.ROOT), player.getName().getString(), ProfessionHelper.getLevel(player, profession))), true);
        return 1;
    }

    private static int addExperience(CommandContext<CommandSourceStack> context, int amount) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        ProfessionHelper.addExperience(player, profession, amount);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§aAdded %d experience to %s for %s. Current level: %d [%d/%d XP]§r",
                amount, profession.name().toLowerCase(Locale.ROOT), player.getName().getString(),
                ProfessionHelper.getLevel(player, profession), ProfessionHelper.getExperience(player, profession),
                ProfessionHelper.getRequiredExperience(player, profession))), true);
        return 1;
    }

    // =========================================================================
    // RESET ACTIONS
    // =========================================================================

    private static int resetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.resetAll(player);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§eReset all profession progress for %s.§r",
                player.getName().getString())), true);
        return 1;
    }

    private static int resetSpecificProfession(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(context, "player");
        ProfessionHelper.Professions profession = parseProfession(context, "profession");

        ProfessionHelper.resetProfession(player, profession);

        context.getSource().sendSuccess(() -> Component.literal(String.format("§eReset %s progress to Level 0 [0 XP] for %s.§r",
                profession.name().toLowerCase(Locale.ROOT), player.getName().getString())), true);
        return 1;
    }
}