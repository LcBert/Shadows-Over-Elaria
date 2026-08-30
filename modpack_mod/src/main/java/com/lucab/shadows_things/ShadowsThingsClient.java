package com.lucab.shadows_things;

import com.lucab.shadows_things.client.DeepCaveEffects;
import com.lucab.shadows_things.client.overlay.ClassExperienceBarOverlay;
import com.lucab.shadows_things.client.renderer.*;
import com.lucab.shadows_things.client.screen.*;
import com.lucab.shadows_things.content.block.cauldron.CauldronRegister;
import com.lucab.shadows_things.content.block.drying_rack.DryingRackRegister;
import com.lucab.shadows_things.content.block.repair_table.RepairTableRegister;
import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarRegistry;
import com.lucab.shadows_things.content.block.resonant.resonant_pedestal.ResonantPedestalRegistry;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.toast.ToastOverlay;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

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
        event.registerBlockEntityRenderer(RepairTableRegister.REPAIR_TABLE_ENTITY.get(), RepairTableRenderer::new);
        event.registerBlockEntityRenderer(CauldronRegister.CAULDRON_BLOCK_ENTITY.get(), CauldronRenderer::new);
        event.registerBlockEntityRenderer(DryingRackRegister.DRYING_RACK_BLOCK_ENTITY.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ResonantPedestalRegistry.RESONANT_PEDESTAL_ENTITY.get(), ResonantPedestalRenderer::new);
        event.registerBlockEntityRenderer(ResonantAltarRegistry.RESONANT_ALTAR_ENTITY.get(), ResonantAltarRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistries.OVEN_MENU.get(), OvenScreen::new);
        event.register(MenuRegistries.SMELTERY_MENU.get(), SmelteryScreen::new);
        event.register(MenuRegistries.CAULDRON_MENU.get(), CauldronScreen::new);
        event.register(MenuRegistries.DRYING_RACK_MENU.get(), DryingRackScreen::new);
        event.register(MenuRegistries.SEEDS_BAG_MENU.get(), SeedsBagScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "toast_overlay"),
                ToastOverlay.INSTANCE
        );

        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_BAR,
                ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "class_experience_bar"),
                new ClassExperienceBarOverlay()
        );
    }
}
