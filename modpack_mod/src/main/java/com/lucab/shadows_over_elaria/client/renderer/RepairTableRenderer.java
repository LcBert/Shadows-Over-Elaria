package com.lucab.shadows_over_elaria.client.renderer;

import com.lucab.shadows_over_elaria.block.repair_table.RepairTableEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RepairTableRenderer implements BlockEntityRenderer<RepairTableEntity> {
    public RepairTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RepairTableEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack item = blockEntity.getItem();
        ItemStack kit = blockEntity.getKit();

        if (!item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1, 0.5);
            poseStack.scale(0.8f, 0.8f, 0.8f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            Minecraft.getInstance().getItemRenderer().renderStatic(item,
                    ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }

        if (!kit.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.7, 1, 0.7);
            poseStack.scale(0.4f, 0.2f, 0.4f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            Minecraft.getInstance().getItemRenderer().renderStatic(kit,
                    ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }
    }

}
