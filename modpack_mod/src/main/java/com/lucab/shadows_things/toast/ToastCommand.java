package com.lucab.shadows_things.toast;

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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ToastCommand {
    private static List<ChatFormatting> BLACKLISTED = List.of(
            ChatFormatting.OBFUSCATED,
            ChatFormatting.BOLD,
            ChatFormatting.STRIKETHROUGH,
            ChatFormatting.UNDERLINE,
            ChatFormatting.ITALIC,
            ChatFormatting.RESET
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("shadow")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("toast")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("text", StringArgumentType.string())
                                                        .then(Commands.argument("color", StringArgumentType.string())
                                                                .suggests(ToastCommand::suggestColors)
                                                                .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                        .executes(ctx -> executeAddToast(ctx, null))
                                                                        .then(Commands.argument("sound", ResourceLocationArgument.id())
                                                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.SOUND_EVENT.keySet(), builder))
                                                                                .executes(ctx -> executeAddToast(ctx, ResourceLocationArgument.getId(ctx, "sound")))))
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static CompletableFuture<Suggestions> suggestColors(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> validColors = Arrays.stream(ChatFormatting.values())
                .filter(format -> !BLACKLISTED.contains(format))
                .map(ChatFormatting::getName)
                .map(String::toUpperCase)
                .toList();
        return SharedSuggestionProvider.suggest(validColors, builder);
    }

    private static int executeAddToast(CommandContext<CommandSourceStack> context, @Nullable ResourceLocation soundId) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String text = StringArgumentType.getString(context, "text");
        String colorName = StringArgumentType.getString(context, "color");
        int duration = IntegerArgumentType.getInteger(context, "duration");

        ChatFormatting color = ChatFormatting.getByName(colorName);
        if (color == null || BLACKLISTED.contains(color)) {
            source.sendFailure(Component.literal("Color not valid or not supported: " + colorName));
            return 0;
        }

        if (soundId != null && !BuiltInRegistries.SOUND_EVENT.containsKey(soundId)) {
            source.sendFailure(Component.literal("Sound event not registered: " + soundId));
            return 0;
        }

        ToastHelper.addToast(player, text, color.getName(), duration, soundId);
        return 1;
    }
}
