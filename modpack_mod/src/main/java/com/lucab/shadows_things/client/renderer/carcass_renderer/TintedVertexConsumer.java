package com.lucab.shadows_things.client.renderer.carcass_renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public record TintedVertexConsumer(
        VertexConsumer parent,
        float r,
        float g,
        float b,
        float a
) implements VertexConsumer {

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        return this.parent.addVertex(x, y, z);
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        int newRed = (int) (red * this.r);
        int newGreen = (int) (green * this.g);
        int newBlue = (int) (blue * this.b);
        int newAlpha = (int) (alpha * this.a);
        return this.parent.setColor(newRed, newGreen, newBlue, newAlpha);
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        return this.parent.setUv(u, v);
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        return this.parent.setUv1(u, v);
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        return this.parent.setUv2(u, v);
    }

    @Override
    public @NotNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        return this.parent.setNormal(normalX, normalY, normalZ);
    }
}