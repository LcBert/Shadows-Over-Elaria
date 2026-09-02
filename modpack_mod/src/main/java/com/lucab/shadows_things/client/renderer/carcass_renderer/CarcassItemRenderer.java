package com.lucab.shadows_things.client.renderer.carcass_renderer;

import com.lucab.shadows_things.content.item.CarcassItem;
import com.lucab.shadows_things.entity.carcas_entity.CarcassEntity;
import com.lucab.shadows_things.recipe.CarcassCuttingRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CarcassItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static CarcassItemRenderer instance;

    // Cache entities per EntityType to avoid allocating instances every frame
    private final Map<EntityType<?>, LivingEntity> cachedEntities = new HashMap<>();

    public CarcassItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static CarcassItemRenderer getInstance() {
        if (instance == null) {
            instance = new CarcassItemRenderer();
        }
        return instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        EntityType<?> type = CarcassItem.getCopiedType(stack);
        if (type == null) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        LivingEntity entity = this.cachedEntities.computeIfAbsent(type, t -> {
            Entity created = t.create(level);
            return (created instanceof LivingEntity living) ? living : null;
        });

        if (entity == null) return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity> renderer = dispatcher.getRenderer(entity);

        // Calculate harvesting progress
        int currentStep = CarcassItem.getInteractions(stack);
        int maxSteps = Math.max(1, CarcassCuttingRecipe.getMaxInteractions(level, type));
        float stepProgress = Mth.clamp((float) currentStep / (float) maxSteps, 0.0F, 1.0F);

        // Calculate rot/decay progress
        long spawnTick = CarcassItem.getSpawnTick(stack);
        float decayProgress = 0.0F;

        if (spawnTick > 0L) {
            long currentTick = level.getGameTime();
            long pastTick = Math.max(0L, currentTick - spawnTick);
            decayProgress = Mth.clamp((float) pastTick / CarcassEntity.MAX_DECAY_TICK, 0.0F, 1.0F);
        }

        // Calculate RGB tint factors identical to entity renderer
        float harvestR = 1.0F - (0.3F * stepProgress);
        float harvestG = 1.0F - (0.75F * stepProgress);
        float harvestB = 1.0F - (0.75F * stepProgress);

        float rotR = 0.4F;
        float rotG = 0.85F;
        float rotB = 0.3F;

        float rTint = Mth.clamp(Mth.lerp(decayProgress, harvestR, harvestR * rotR), 0.0F, 1.0F);
        float gTint = Mth.clamp(Mth.lerp(decayProgress, harvestG, harvestG * rotG), 0.0F, 1.0F);
        float bTint = Mth.clamp(Mth.lerp(decayProgress, harvestB, harvestB * rotB), 0.0F, 1.0F);

        MultiBufferSource tintedBufferSource = renderType -> new TintedVertexConsumer(
                bufferSource.getBuffer(renderType),
                rTint,
                gTint,
                bTint,
                1.0F
        );

        poseStack.pushPose();

        // 1. Context Transformations (GUI, First-Person, Third-Person, Ground)
        applyTransformations(displayContext, poseStack, entity);

        // 2. Freeze animation state
        entity.walkAnimation.setSpeed(0.0F);
        entity.walkAnimation.position(0.0F);
        entity.hurtTime = 0;
        entity.deathTime = 0;
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        entity.yHeadRot = 0.0F;
        entity.yHeadRotO = 0.0F;

        // 3. Render model directly
        renderer.render(
                entity,
                0.0F,
                1.0F,
                poseStack,
                tintedBufferSource,
                packedLight
        );

        poseStack.popPose();
    }

    private void applyTransformations(ItemDisplayContext context, PoseStack poseStack, LivingEntity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        float maxDim = Math.max(width, height);
        float scale = (maxDim > 0.0F) ? (0.6F / maxDim) : 0.6F;

        switch (context) {
            case GUI -> {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(scale, scale, scale);

                // Pitch angle to see the model from slightly above
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));

                // 225 degrees (or -135) to orient the model facing South-East (towards the screen bottom-right)
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));

                // Re-center model origin inside the GUI slot
                poseStack.translate(0.0D, -(height * 0.5D), 0.0D);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5D, 0.5D, 0.3D);
                poseStack.scale(scale * 0.7F, scale * 0.7F, scale * 0.7F);
                poseStack.translate(0.0D, -(height * 0.5D), 0.0D);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.translate(0.5D, 0.3D, 0.5D);
                poseStack.scale(scale * 0.55F, scale * 0.55F, scale * 0.55F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(0.0D, -(height * 0.5D), 0.0D);
            }
            case GROUND -> {
                poseStack.translate(0.5D, 0.3D, 0.5D);
                poseStack.scale(scale * 0.75F, scale * 0.75F, scale * 0.75F);
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            }
            case FIXED, HEAD -> {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(scale, scale, scale);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(0.0D, -(height * 0.5D), 0.0D);
            }
            default -> {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0D, -(height * 0.5D), 0.0D);
            }
        }
    }
}