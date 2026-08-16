package com.lucab.shadows_things.client.renderer;

import com.lucab.shadows_things.content.block.cauldron.CauldronBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class CauldronRenderer implements BlockEntityRenderer<CauldronBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CauldronBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Render Items
        IItemHandler inventory = blockEntity.getInventoryHandler();
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots() - 1; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) stacks.add(inventory.getStackInSlot(i));
        }

        if (!stacks.isEmpty()) {
            renderInventoryItems(stacks, blockEntity, poseStack, bufferSource, packedLight, packedOverlay, partialTick);
        }

        // Render water/potion
        PotionContents potionContents = blockEntity.getPotionContents();
        if (potionContents != null && !potionContents.equals(PotionContents.EMPTY)) {
            renderWater(blockEntity, potionContents, poseStack, bufferSource, packedOverlay);
        }
    }

    private void renderInventoryItems(List<ItemStack> stacks, CauldronBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick) {
        int count = stacks.size();

        // Calculate water height to place items slightly above or directly on the water level
        int waterLevel = blockEntity.getWaterLevel();
        float itemHeight = (6.0f + (4.0f * (waterLevel - 1)) + 0.5f) / 16.0f; // Slightly above water surface

        boolean isProcessing = blockEntity.getProcessTime() > 0;
        float globalRotationAngle = 0.0f;

        if (isProcessing && blockEntity.getLevel() != null) {
            float time = blockEntity.getLevel().getGameTime() + partialTick;
            globalRotationAngle = time * 8.0f;
        }

        poseStack.pushPose();

        poseStack.translate(0.5f, itemHeight, 0.5f);
        if (isProcessing) {
            poseStack.mulPose(Axis.YP.rotationDegrees(globalRotationAngle));
        }

        for (int i = 0; i < count; i++) {
            ItemStack stack = stacks.get(i);
            poseStack.pushPose();

            // Distribute items in a circle layout inside the cauldron
            float radius = 0.2f; // Distance from center
            double angle = (2.0 * Math.PI / count) * i;
            float xOffset = (float) (Math.cos(angle) * radius);
            float zOffset = (float) (Math.sin(angle) * radius);

            poseStack.translate(xOffset, 0.0f, zOffset);
            poseStack.scale(0.35f, 0.35f, 0.35f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * (360.0f / count)));

            this.itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    i
            );

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderWater(CauldronBlockEntity blockEntity, PotionContents potionContents, PoseStack poseStack, MultiBufferSource bufferSource, int packedOverlay) {
        poseStack.pushPose();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());

        FluidState fluidState = Fluids.WATER.defaultFluidState();
        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidState);

        TextureAtlasSprite waterSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidExtensions.getStillTexture());

        int rgb = potionContents.getColor();
        int color = (rgb == -1) ? 0xFF3F76E4 : (rgb | 0xFF000000);

        int light = getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos());

        int waterLevel = blockEntity.getWaterLevel();
        float heightInPixel = 6.0f + (4.0f * (waterLevel - 1));
        float waterHeight = heightInPixel / 16.0f;

        renderWaterQuad(poseStack, vertexConsumer, waterSprite, 0.125f, waterHeight, 0.125f, 0.875f, waterHeight, 0.875f, color, light, packedOverlay);

        poseStack.popPose();
    }

    private int getLightLevel(net.minecraft.world.level.Level level, BlockPos pos) {
        if (level == null) return 15728880;
        return LevelRenderer.getLightColor(level, pos.above());
    }

    private void renderWaterQuad(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite sprite,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 int color, int light, int overlay) {
        PoseStack.Pose pose = poseStack.last();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        addVertex(consumer, pose, x1, y2, z1, color, u0, v0, light, overlay);
        addVertex(consumer, pose, x1, y2, z2, color, u0, v1, light, overlay);
        addVertex(consumer, pose, x2, y2, z2, color, u1, v1, light, overlay);
        addVertex(consumer, pose, x2, y2, z1, color, u1, v0, light, overlay);
    }

    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                           int color, float u, float v, int light, int overlay) {
        int alpha = FastColor.ARGB32.alpha(color);
        int red = FastColor.ARGB32.red(color);
        int green = FastColor.ARGB32.green(color);
        int blue = FastColor.ARGB32.blue(color);

        consumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}