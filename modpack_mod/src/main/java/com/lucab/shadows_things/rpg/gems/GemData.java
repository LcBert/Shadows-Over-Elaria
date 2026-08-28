package com.lucab.shadows_things.rpg.gems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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


    public static ResourceLocation getGemId(ItemStack stack) {
        if (!stack.has(SocketRegistries.GEM_DATA)) return null;
        return stack.get(SocketRegistries.GEM_DATA).gemId();
    }

    public static int getTier(ItemStack stack) {
        if (!stack.has(SocketRegistries.GEM_DATA)) return -1;
        return stack.get(SocketRegistries.GEM_DATA).rarity();
    }
}
