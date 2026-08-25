package com.lucab.shadows_things.client.renderer;

import com.lucab.shadows_things.content.block.drying_rack.DryingRackBlock;
import com.lucab.shadows_things.content.block.drying_rack.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DryingRackBlockEntity rackEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = rackEntity.getLevel();
        if (level == null) return;
        BlockState state = level.getBlockState(rackEntity.getBlockPos());
        if (!(state.getBlock() instanceof DryingRackBlock rackBlock)) return;

        Direction facing = state.getValue(DryingRackBlock.FACING);

        for (int i = 0; i < 5; i++) {
            ItemStack stack = rackEntity.getInventoryHandler().getStackInSlot(i);
            if (stack.isEmpty()) continue;

            poseStack.pushPose();

            poseStack.translate(0.5, 0.0, 0.5);
            float angle = switch (facing) {
                case SOUTH -> 180.0f;
                case WEST -> 90.0f;
                case EAST -> 270.0f;
                default -> 0.0f; // NORTH
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));

            float hookSpacing = 3.0f / 16.0f;
            float startOffset = -0.375f;

            float localX = startOffset + (i * hookSpacing);
            float localY = 0.65f;
            float localZ = 0.0f;

            poseStack.translate(localX, localY, localZ);

            poseStack.scale(0.25f, 0.25f, 0.25f);

            Minecraft.getInstance().getItemRenderer().renderStatic(stack,
                    ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, rackEntity.getLevel(), 1);
            poseStack.popPose();
        }
    }
}
