package com.lucab.shadows_things.network;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.ProfessionMenu;
import com.lucab.shadows_things.rpg.professions.ProfessionContainerData;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenProfessionGuiPacket() implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "open_profession_gui_packet");
    public static final Type<OpenProfessionGuiPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenProfessionGuiPacket> STREAM_CODEC = StreamCodec.unit(new OpenProfessionGuiPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenProfessionGuiPacket packet, IPayloadContext context) {
        Player player = context.player();
        context.enqueueWork(() -> {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> {
                return new ProfessionMenu(id, inv, new ProfessionContainerData(player));
            }, Component.literal("Profession")));
        });
    }
}
