package com.lucab.shadows_things.rpg.gems;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Optional;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class SocketTooltip {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        int maxSockets = SocketManager.getMaxSocketsForItem(stack);

        if (maxSockets > 0) {
            SocketDataComponent socketData = SocketManager.getOrCreateSocketData(stack);

            event.getToolTip().add(Component.literal("Sockets:").withStyle(ChatFormatting.GOLD));

            int currentGems = socketData.gems().size();

            for (int i = 0; i < currentGems; i++) {
                GemSocket socket = socketData.gems().get(i);
                GemData gemData = socket.gemData();

                Optional<GemDefinition> defOpt = GemDataReader.get(gemData.gemId());
                String gemName = defOpt.isPresent() ? defOpt.get().name() : gemData.gemId().getPath();

                event.getToolTip().add(Component.literal("  [" + gemName + " - Tier " + gemData.rarity() + "]").withStyle(ChatFormatting.GREEN));
            }

            for (int i = currentGems; i < maxSockets; i++) {
                event.getToolTip().add(Component.literal("  [] Empty Socket").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
