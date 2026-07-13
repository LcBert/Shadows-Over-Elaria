package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class XpRemoval {
    private static final List<ResourceLocation> movedLayers = List.of(
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH
    );

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        ResourceLocation layer = event.getName();
        if (layer.equals(VanillaGuiLayers.EXPERIENCE_BAR)) event.setCanceled(true);
        if (layer.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) event.setCanceled(true);

        if (movedLayers.contains(layer)) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(0, 6, 0);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        ResourceLocation layer = event.getName();
        if (movedLayers.contains(layer)) event.getGuiGraphics().pose().popPose();
    }

    @SubscribeEvent
    public static void xpSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ExperienceOrb) event.setCanceled(true);
    }
}
