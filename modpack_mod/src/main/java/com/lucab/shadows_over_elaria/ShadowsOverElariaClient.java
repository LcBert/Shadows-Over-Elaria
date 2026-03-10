package com.lucab.shadows_over_elaria;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.lucab.shadows_over_elaria.block.BlocksRegister;
import com.lucab.shadows_over_elaria.client.renderer.RepairTableRenderer;

@Mod(value = ShadowsOverElaria.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ShadowsOverElaria.MODID, value = Dist.CLIENT)
public class ShadowsOverElariaClient {
    public ShadowsOverElariaClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlocksRegister.REPAIR_TABLE_ENTITY.get(),
                RepairTableRenderer::new);
    }
}
