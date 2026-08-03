package com.lucab.shadows_things.toast;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToastPacket(String text, String color, int duration) implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "toast_packet");
    public static final Type<ToastPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ToastPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ToastPacket::text,
            ByteBufCodecs.STRING_UTF8,
            ToastPacket::color,
            ByteBufCodecs.INT,
            ToastPacket::duration,
            ToastPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ChatFormatting color = ChatFormatting.getByName(packet.color());
            if (color == null) color = ChatFormatting.WHITE;
            ToastClientHelper.addToast(packet.text, color, packet.duration);
        });
    }
}
