package com.lucab.shadows_things.rpg.gems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record GemData(ResourceLocation gemId, int rarity) {
    public static final Codec<GemData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(GemData::gemId),
                    Codec.INT.fieldOf("rarity").forGetter(GemData::rarity)
            ).apply(instance, GemData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GemData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GemData::gemId,
            ByteBufCodecs.VAR_INT, GemData::rarity,
            GemData::new
    );
}
