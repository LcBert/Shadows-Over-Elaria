package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public record SyncDungeonHighlightsPayload(List<AABB> roomBoxes) implements CustomPacketPayload {
    public static final Type<SyncDungeonHighlightsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "sync_dungeon_highlights"));

    public static final StreamCodec<ByteBuf, AABB> AABB_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, box -> box.minX,
            ByteBufCodecs.DOUBLE, box -> box.minY,
            ByteBufCodecs.DOUBLE, box -> box.minZ,
            ByteBufCodecs.DOUBLE, box -> box.maxX,
            ByteBufCodecs.DOUBLE, box -> box.maxY,
            ByteBufCodecs.DOUBLE, box -> box.maxZ,
            AABB::new
    );

    public static final StreamCodec<ByteBuf, SyncDungeonHighlightsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, AABB_STREAM_CODEC),
            SyncDungeonHighlightsPayload::roomBoxes,
            SyncDungeonHighlightsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}