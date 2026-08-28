package com.lucab.shadows_things.client.renderer;

import com.lucab.shadows_things.content.block.resonant.resonant_pedestal.ResonantPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResonantPedestalRenderer implements BlockEntityRenderer<ResonantPedestalBlockEntity> {
    public ResonantPedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResonantPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack item = blockEntity.getItem();

        if (!item.isEmpty()) {
            Level level = blockEntity.getLevel();
            if (level == null) return;

            poseStack.pushPose();

            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.scale(0.6F, 0.6F, 0.6F);

            float rotationAngle = -(level.getGameTime() + partialTick) * 4.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

            Minecraft.getInstance().getItemRenderer().renderStatic(item,
                    ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, level, (int) blockEntity.getBlockPos().asLong());

            poseStack.popPose();
        }
    }
}
