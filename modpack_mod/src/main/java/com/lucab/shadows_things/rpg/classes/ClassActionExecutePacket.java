package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClassActionExecutePacket(int actionType) implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "class_action_execute_packet");
    public static final Type<ClassActionExecutePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClassActionExecutePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClassActionExecutePacket::actionType,
            ClassActionExecutePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClassActionExecutePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ClassManager.executeAction(player, packet.actionType);
        });
    }
}
