package com.lucab.shadows_things.network;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpgradeProfessionPacket(String name) implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "upgrade_profession_packet");
    public static final Type<UpgradeProfessionPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeProfessionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpgradeProfessionPacket::name,
            UpgradeProfessionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpgradeProfessionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ProfessionHelper.Professions prof = ProfessionHelper.Professions.valueOf(packet.name.toUpperCase());
            ShadowsThings.LOGGER.info(String.valueOf(ProfessionHelper.canUpgradeProfession(player, prof)));
            if (ProfessionHelper.canUpgradeProfession(player, prof)) {
                ProfessionHelper.incrementLevel(player, prof);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        });
    }
}
