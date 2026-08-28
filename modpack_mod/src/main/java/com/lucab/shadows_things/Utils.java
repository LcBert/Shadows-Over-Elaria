package com.lucab.shadows_things;

import com.lucab.shadows_things.content.block.repair_table.RepairType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isRepairKit(ItemStack stack) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "repair_kits")));
    }

    public static boolean isRepairHammer(ItemStack stack) {
        return stack
                .is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "repair_hammers")));
    }

    public static boolean isItemToRepair(ItemStack stack) {
        for (RepairType value : RepairType.values()) {
            Item valueItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(value.item));
            if (stack.getItem() == valueItem)
                return true;
        }
        return false;
    }

    public static void sendTitleMessage(Player player, Component title, @Nullable Component subtitle) {
        ClientboundSetTitleTextPacket titleMessage = new ClientboundSetTitleTextPacket(title);
        ClientboundSetSubtitleTextPacket subtitleMessage = null;
        if (subtitle != null)
            subtitleMessage = new ClientboundSetSubtitleTextPacket(subtitle);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(titleMessage);
            if (subtitle != null) serverPlayer.connection.send(subtitleMessage);
        }
    }

    public static List<Vec3> getEquidistantPoints(BlockPos startPos, BlockPos endPos, int count) {
        List<Vec3> points = new ArrayList<>(Math.max(count, 0));

        if (count <= 0) return points;

        Vec3 start = startPos.getCenter();
        if (count == 1) {
            points.add(start);
            return points;
        }

        Vec3 end = endPos.getCenter();
        double step = 1.0 / (count - 1);

        for (int i = 0; i < count; i++) {
            double delta = i * step;
            points.add(start.lerp(end, delta));
        }

        return points;
    }
}
