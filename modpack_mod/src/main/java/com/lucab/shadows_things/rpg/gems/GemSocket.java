package com.lucab.shadows_things.rpg.gems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record GemSocket(GemData gemData) {
    public static final Codec<GemSocket> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    GemData.CODEC.fieldOf("gem_data").forGetter(GemSocket::gemData)
            ).apply(instance, GemSocket::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GemSocket> STREAM_CODEC = StreamCodec.composite(
            GemData.STREAM_CODEC, GemSocket::gemData,
            GemSocket::new
    );
}
