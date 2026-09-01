package com.lucab.shadows_things.client.renderer;

import com.lucab.shadows_things.entity.carcas_entity.CarcassEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class CarcassEntityRender extends EntityRenderer<CarcassEntity> {
    private final EntityRenderDispatcher dispatcher;

    public CarcassEntityRender(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(CarcassEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int currentStep = entity.getCurrentInteractions();
        int maxSteps = Math.max(1, entity.getMaxSteps());
        float stepProgress = Mth.clamp((float) currentStep / (float) maxSteps, 0.0F, 1.0F);

        float rTint = 1.0F - (0.3F * stepProgress);
        float gTint = 1.0F - (0.8F * stepProgress);
        float bTint = 1.0F - (0.8F * stepProgress);

        MultiBufferSource tintedBufferSource = renderType -> new TintedVertexConsumer(
                bufferSource.getBuffer(renderType),
                rTint,
                gTint,
                bTint,
                1.0F
        );

        LivingEntity delegate = entity.getOrCreateClientEntity();
        if (delegate != null) {
            EntityRenderer<? super LivingEntity> renderer = this.dispatcher.getRenderer(delegate);

            delegate.walkAnimation.setSpeed(0.0F);
            delegate.walkAnimation.position(0.0F);
            delegate.deathTime = 0;
            delegate.hurtTime = 0;

            delegate.setYRot(0.0F);
            delegate.setXRot(0.0F);
            delegate.yBodyRot = 0.0F;
            delegate.yHeadRot = 0.0F;
            delegate.yRotO = 0.0F;
            delegate.xRotO = 0.0F;

            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

            float height = delegate.getBbHeight();
            float width = delegate.getBbWidth();

            poseStack.translate(0.0D, width * 0.5D, 0.0D);

            poseStack.translate(height * 0.5D, 0.0D, 0.0D);

            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));

            renderer.render(
                    delegate,
                    0.0F,
                    partialTick,
                    poseStack,
                    tintedBufferSource,
                    packedLight
            );

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, tintedBufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CarcassEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private record TintedVertexConsumer(
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
}
