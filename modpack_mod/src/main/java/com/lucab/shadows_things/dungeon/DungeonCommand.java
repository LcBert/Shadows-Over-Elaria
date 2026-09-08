package com.lucab.shadows_things.dungeon;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

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
                                .then(Commands.literal("debug")
                                        .then(Commands.literal("currentRoom")
                                                .executes(DungeonCommand::debugCurrentRoom))
                                        .then(Commands.literal("highlight")
                                                .executes(DungeonCommand::debugHighlightRooms))
                                )
                        )
        );
    }

    private static int getActive(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        int activeInstances = DungeonManager.getDungeonInstances().size();

        source.sendSuccess(() -> Component.literal("Currently active instances: " + activeInstances).withStyle(ChatFormatting.GOLD), false);

        return activeInstances;
    }

    private static int teleportPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        ServerPlayer singlePlayer = players.size() == 1 ? players.iterator().next() : null;

        DungeonInstance instance = DungeonManager.createDungeonInstance();
        if (instance == null) {
            source.sendFailure(Component.literal("Failed to create dungeon instance: Level not available"));
            return 0;
        }

        instance.addPlayers(players.stream().map(Player::getUUID).toList());

        source.sendSuccess(() -> Component.literal("Dungeon is generating, please wait...").withStyle(ChatFormatting.GOLD), false);
        instance.prepareAndTeleportPlayers(null, null).thenRun(() -> {
            if (singlePlayer == null) {
                source.sendSuccess(() -> Component.literal(String.format("Teleported %s players into dungeon", players.size())).withStyle(ChatFormatting.GREEN), false);
            } else {
                source.sendSuccess(() -> Component.literal(String.format("Teleported %s into dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GREEN), false);
            }
        });

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
                source.sendSuccess(() -> Component.literal(String.format("Exited %s players from dungeon", trueCount)).withStyle(ChatFormatting.GREEN), false);
            if (falseCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("%s players are not in dungeon", falseCount)).withStyle(ChatFormatting.GOLD), false);
        } else {
            if (trueCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("Exited %s from dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GREEN), false);
            if (falseCount > 0)
                source.sendSuccess(() -> Component.literal(String.format("%s is not in dungeon", singlePlayer.getName().getString())).withStyle(ChatFormatting.GOLD), false);
        }

        return players.size();
    }

    private static int debugCurrentRoom(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);

        if (instance == null) {
            context.getSource().sendFailure(Component.literal("You are not inside an active dungeon instance"));
            return 0;
        }

        DungeonRoom currentRoom = instance.getRoomAtWorld(player.blockPosition());
        if (currentRoom == null) {
            context.getSource().sendFailure(Component.literal("You are inside the dungeon bounding box, but outside any indexed room bounds").withStyle(ChatFormatting.GOLD));
            return 0;
        }

        Vec3i gp = currentRoom.getGridPos();
        String template = currentRoom.getTemplateLocation() != null ? currentRoom.getTemplateLocation().toString() : "NONE";

        context.getSource().sendSuccess(() -> Component.literal(
                String.format("Current Room: Grid [%d, %d, %d] | Template: %s | Origin: %s",
                        gp.getX(), gp.getY(), gp.getZ(), template, currentRoom.getOriginPos().toShortString())
        ).withStyle(ChatFormatting.AQUA), false);

        return 1;
    }

    private static int debugHighlightRooms(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        DungeonInstance instance = DungeonManager.getInstanceForPlayer(player);

        if (instance == null) {
            context.getSource().sendFailure(Component.literal("You must be inside an active dungeon instance"));
            return 0;
        }

        boolean active = DungeonHandler.toggleHighlight(player.getUUID());
        List<AABB> boxes = active
                ? instance.getRooms().stream().map(DungeonRoom::getBoundingBox).toList()
                : List.of();

        PacketDistributor.sendToPlayer(player, new SyncDungeonHighlightsPayload(boxes));

        context.getSource().sendSuccess(() -> Component.literal("Dungeon room panels outline: " + (active ? "ENABLED" : "DISABLED"))
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED), false);

        return 1;
    }
}
