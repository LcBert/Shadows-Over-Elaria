package com.lucab.shadows_things.client.renderer;

import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ResonantAltarRenderer implements BlockEntityRenderer<ResonantAltarBlockEntity> {

    public ResonantAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static final double[] CORNER_OFFSETS = new double[]{0.125D, 0.875D};
    private static final double BASE_REAGENT_Y = 1.15D;

    private static final Vec3[] IDLE_REAGENT_POSITIONS = new Vec3[]{
            new Vec3(CORNER_OFFSETS[0], BASE_REAGENT_Y, CORNER_OFFSETS[0]),
            new Vec3(CORNER_OFFSETS[1], BASE_REAGENT_Y, CORNER_OFFSETS[0]),
            new Vec3(CORNER_OFFSETS[1], BASE_REAGENT_Y, CORNER_OFFSETS[1]),
            new Vec3(CORNER_OFFSETS[0], BASE_REAGENT_Y, CORNER_OFFSETS[1]),
    };

    @Override
    public void render(ResonantAltarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        float time = level.getGameTime() + partialTick;
        float progress = blockEntity.getInterpolatedProgress(partialTick);
        boolean isCrafting = progress > 0.0F;

        // 1. Center Base Item / Tool Rendering
        renderCenterItem(blockEntity, time, isCrafting, progress, poseStack, bufferSource, packedLight, packedOverlay);

        // 2. Reagents Rendering
        List<ItemStack> reagents = blockEntity.getReagents();
        if (!reagents.isEmpty()) {
            if (isCrafting) {
                renderCraftingReagents(reagents, time, progress, poseStack, bufferSource, level, packedLight, packedOverlay);
            } else {
                renderIdleReagents(reagents, time, poseStack, bufferSource, level, packedLight, packedOverlay);
            }
        }
    }

    private void renderCenterItem(ResonantAltarBlockEntity blockEntity, float time, boolean isCrafting, float progress,
                                  PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack tool = blockEntity.getTool();
        if (tool.isEmpty()) return;

        poseStack.pushPose();

        float bobOffset = isCrafting ? Mth.sin(time * 0.2F) * 0.05F + (progress * 0.15F) : 0.0F;
        poseStack.translate(0.5D, 0.85D + bobOffset, 0.5D);

        float scale = 0.5F + (isCrafting ? Mth.sin(time * 0.4F) * 0.03F : 0.0F);
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        if (isCrafting) {
            // Smooth continuous spin that scales smoothly without angular jumps
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * 8.0F));
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(
                tool,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                1
        );

        poseStack.popPose();
    }

    private void renderIdleReagents(List<ItemStack> reagents, float time, PoseStack poseStack,
                                    MultiBufferSource bufferSource, Level level, int packedLight, int packedOverlay) {
        for (int i = 0; i < reagents.size(); i++) {
            ItemStack reagent = reagents.get(i);
            if (reagent.isEmpty()) continue;

            Vec3 pos = IDLE_REAGENT_POSITIONS[i % IDLE_REAGENT_POSITIONS.length];
            float bobY = Mth.sin((time * 0.1F) + (i * 1.5F)) * 0.03F;

            poseStack.pushPose();
            poseStack.translate(pos.x, pos.y + bobY, pos.z);
            poseStack.scale(0.25F, 0.25F, 0.25F);

            poseStack.mulPose(Axis.YP.rotationDegrees((time * 2.5F) + (i * 90.0F)));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    reagent,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    level,
                    i + 10
            );

            poseStack.popPose();
        }
    }

    private void renderCraftingReagents(List<ItemStack> reagents, float time, float progress,
                                        PoseStack poseStack, MultiBufferSource bufferSource, Level level, int packedLight, int packedOverlay) {
        int count = reagents.size();

        // Stable orbit base angle without multiplying varying speed over total elapsed time
        double baseOrbitAngle = (time * 0.12D);

        // Smooth interpolated radius and elevation
        float currentRadius = Mth.lerp(progress, 0.42F, 0.18F);
        float heightBase = (float) BASE_REAGENT_Y + (progress * 0.35F);

        for (int i = 0; i < count; i++) {
            ItemStack reagent = reagents.get(i);
            if (reagent.isEmpty()) continue;

            double angle = baseOrbitAngle + (i * (2.0D * Math.PI / count));

            double posX = 0.5D + Math.cos(angle) * currentRadius;
            double posZ = 0.5D + Math.sin(angle) * currentRadius;
            double posY = heightBase + (Mth.sin((float) (time * 0.25D + (i * 1.57D))) * 0.06F);

            poseStack.pushPose();
            poseStack.translate(posX, posY, posZ);

            float itemScale = Mth.lerp(progress, 0.25F, 0.15F);
            poseStack.scale(itemScale, itemScale, itemScale);

            // Item orientation follows orbit trajectory smoothly
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(-angle) + 90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0F + (progress * 30.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * 16.0F));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    reagent,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    level,
                    i + 20
            );

            poseStack.popPose();
        }
    }
}