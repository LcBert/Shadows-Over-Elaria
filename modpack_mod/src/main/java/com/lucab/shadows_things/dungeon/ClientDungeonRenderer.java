package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class ClientDungeonRenderer {
    private static final double PANEL_INSET = 0.05D;
    private static final float PANEL_ALPHA = 0.18F;

    // Face color mapping:
    // DOWN  (Y-) -> Red
    // UP    (Y+) -> Green
    // NORTH (Z-) -> Blue
    // SOUTH (Z+) -> Yellow
    // WEST  (X-) -> Magenta
    // EAST  (X+) -> Cyan
    private static final float[][] FACE_COLORS = {
            {0.95F, 0.20F, 0.20F}, // DOWN
            {0.20F, 0.95F, 0.20F}, // UP
            {0.20F, 0.40F, 0.95F}, // NORTH
            {0.95F, 0.90F, 0.20F}, // SOUTH
            {0.90F, 0.20F, 0.90F}, // WEST
            {0.20F, 0.90F, 0.90F}  // EAST
    };

    // Wireframe edge styling (subtle neutral white/gray to frame colors)
    private static final float EDGE_RED = 1.00F;
    private static final float EDGE_GREEN = 1.00F;
    private static final float EDGE_BLUE = 1.00F;
    private static final float EDGE_ALPHA = 0.70F;

    private static final List<AABB> HIGHLIGHTED_ROOMS = new ArrayList<>();
    private static boolean active = false;

    private static final RenderType TRANSLUCENT_PANELS = RenderType.create(
            ShadowsThings.MODID + "_room_panels",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static synchronized void updateHighlightedRooms(List<AABB> rooms) {
        HIGHLIGHTED_ROOMS.clear();
        HIGHLIGHTED_ROOMS.addAll(rooms);
        active = !rooms.isEmpty();
    }

    public static synchronized void clear() {
        HIGHLIGHTED_ROOMS.clear();
        active = false;
    }

    public static void toggle() {
        active = !active;
    }

    public static boolean isActive() {
        return active;
    }

    public static List<AABB> getHighlightedRooms() {
        return Collections.unmodifiableList(HIGHLIGHTED_ROOMS);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!active || HIGHLIGHTED_ROOMS.isEmpty()) {
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f pose = poseStack.last().pose();

        // 1. Render flat translucent quad panels with dedicated face colors
        VertexConsumer panelConsumer = bufferSource.getBuffer(TRANSLUCENT_PANELS);
        for (AABB box : HIGHLIGHTED_ROOMS) {
            renderBoxPanels(pose, panelConsumer, box, PANEL_ALPHA);
        }

        // 2. Render wireframe edges
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        for (AABB box : HIGHLIGHTED_ROOMS) {
            LevelRenderer.renderLineBox(poseStack, lineConsumer, box, EDGE_RED, EDGE_GREEN, EDGE_BLUE, EDGE_ALPHA);
        }

        poseStack.popPose();

        bufferSource.endBatch(TRANSLUCENT_PANELS);
        bufferSource.endBatch(RenderType.lines());
    }

    /**
     * Renders each of the 6 bounding box faces with a unique directional color.
     */
    private static void renderBoxPanels(Matrix4f pose, VertexConsumer consumer, AABB originalBox, float a) {
        AABB box = originalBox.deflate(PANEL_INSET);

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // DOWN (Y-) -> Red
        float[] c = FACE_COLORS[0];
        consumer.addVertex(pose, minX, minY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(c[0], c[1], c[2], a);

        // UP (Y+) -> Green
        c = FACE_COLORS[1];
        consumer.addVertex(pose, minX, maxY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(c[0], c[1], c[2], a);

        // NORTH (Z-) -> Blue
        c = FACE_COLORS[2];
        consumer.addVertex(pose, minX, minY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, minY, minZ).setColor(c[0], c[1], c[2], a);

        // SOUTH (Z+) -> Yellow
        c = FACE_COLORS[3];
        consumer.addVertex(pose, minX, minY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(c[0], c[1], c[2], a);

        // WEST (X-) -> Magenta
        c = FACE_COLORS[4];
        consumer.addVertex(pose, minX, minY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, minY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, maxY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, minX, maxY, minZ).setColor(c[0], c[1], c[2], a);

        // EAST (X+) -> Cyan
        c = FACE_COLORS[5];
        consumer.addVertex(pose, maxX, minY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, minZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, maxY, maxZ).setColor(c[0], c[1], c[2], a);
        consumer.addVertex(pose, maxX, minY, maxZ).setColor(c[0], c[1], c[2], a);
    }
}