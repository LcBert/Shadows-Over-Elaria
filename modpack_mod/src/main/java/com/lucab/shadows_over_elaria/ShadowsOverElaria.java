package com.lucab.shadows_over_elaria;

import org.slf4j.Logger;

import com.lucab.shadows_over_elaria.item.Crops;
import com.lucab.shadows_over_elaria.item.Plates;
import com.lucab.shadows_over_elaria.item.RepairKits;
import com.lucab.shadows_over_elaria.item.Rods;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ShadowsOverElaria.MODID)
public class ShadowsOverElaria {
    public static final String MODID = "shadows_over_elaria";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public ShadowsOverElaria(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        ITEMS.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        Crops.register();
        Plates.register();
        Rods.register();
        RepairKits.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
