package com.lucab.shadows_things;

import com.lucab.shadows_things.client.DeepCaveEffects;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.client.renderer.RepairTableRenderer;

import com.lucab.shadows_things.client.screen.OvenScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ShadowsThings.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class ShadowsThingsClient {
    public ShadowsThingsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "deep_cave_effects"), new DeepCaveEffects());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlocksRegister.REPAIR_TABLE_ENTITY.get(),
                RepairTableRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistries.OVEN_MENU.get(), OvenScreen::new);
    }
}
