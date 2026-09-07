package com.lucab.shadows_things.dungeon;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;

public class DungeonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shadow")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("dungeon")
                                .then(Commands.literal("getActive")
                                        .executes(DungeonCommand::getActive))
                                .then(Commands.literal("teleport")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(DungeonCommand::teleportPlayers)))
                                .then(Commands.literal("exit")
                                        .then(Commands.argument("players", EntityArgument.players())
                                                .executes(DungeonCommand::exitPlayers)))
                        )
        );
    }

    private static int getActive(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        int activeInstances = DungeonManager.getDungeonInstances().size();

        source.sendSuccess(() -> Component.literal("Currently active instances: " + activeInstances).withStyle(ChatFormatting.GOLD), true);

        return activeInstances;
    }

    private static int teleportPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        ServerPlayer singlePlayer = players.size() == 1 ? players.iterator().next() : null;

        DungeonInstance instance = DungeonManager.createDungeonInstance();

        instance.addPlayers(players.stream().map(Player::getUUID).toList());

        instance.teleportPlayers(null, null);

        if (singlePlayer == null) {
            source.sendSuccess(() -> Component.literal(String.format("Teleported %s players into dungeon", players.size())).withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("Teleported %s into dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GREEN), true);
        }

        return players.size();
    }

    private static int exitPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        ServerPlayer singlePlayer = players.size() == 1 ? players.iterator().next() : null;

        List<Boolean> results = DungeonManager.exitPlayers(players.stream().map(Player::getUUID).toList());

        long trueCount = results.stream().filter(c -> c).count();
        long falseCount = results.stream().filter(c -> !c).count();

        if (singlePlayer == null) {
            if (trueCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("Exited %s players from dungeon", trueCount)).withStyle(ChatFormatting.GREEN), true);
            if (falseCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("%s players are not in dungeon", falseCount)).withStyle(ChatFormatting.GOLD), true);
        } else {
            if (trueCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("Exited %s from dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GREEN), true);
            if (falseCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("%s is not in dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GOLD), true);
        }

        return players.size();
    }
}
