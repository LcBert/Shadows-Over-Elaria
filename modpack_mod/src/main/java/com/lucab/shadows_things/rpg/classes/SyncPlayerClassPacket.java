package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.client.screen.classes.ClassScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerClassPacket(String className, int tier) implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "sync_player_class_packet");
    public static final Type<SyncPlayerClassPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerClassPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPlayerClassPacket::className,
            ByteBufCodecs.INT, SyncPlayerClassPacket::tier,
            SyncPlayerClassPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerClassPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientClassDataHolder.setPlayerClass(packet.className(), packet.tier());

            if (!ClientClassDataHolder.hasClass() && !(Minecraft.getInstance().screen instanceof ClassScreen)) {
                Minecraft.getInstance().setScreen(new ClassScreen());
            }
        });
    }
}
