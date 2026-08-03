package com.lucab.shadows_things.rpg.gems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record SocketDataComponent(int maxSockets, List<GemSocket> gems) {
    public static final Codec<SocketDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("max_sockets").forGetter(SocketDataComponent::maxSockets),
                    GemSocket.CODEC.listOf().fieldOf("gems").forGetter(SocketDataComponent::gems)
            ).apply(instance, SocketDataComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SocketDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SocketDataComponent::maxSockets,
            GemSocket.STREAM_CODEC.apply(ByteBufCodecs.list()), SocketDataComponent::gems,
            SocketDataComponent::new
    );

    public boolean canInsertGem() {
        return gems.size() < maxSockets;
    }

    public SocketDataComponent withGem(GemSocket gem) {
        List<GemSocket> newGems = new ArrayList<>(gems);
        if (newGems.size() < maxSockets) {
            newGems.add(gem);
        }
        return new SocketDataComponent(maxSockets, newGems);
    }

    public SocketDataComponent withMaxSockets(int maxSockets) {
        return new SocketDataComponent(maxSockets, gems);
    }
}