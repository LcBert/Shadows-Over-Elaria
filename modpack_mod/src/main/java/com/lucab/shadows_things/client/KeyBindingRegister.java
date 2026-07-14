package com.lucab.shadows_things.client;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.ProfessionMenu;
import com.lucab.shadows_things.network.OpenProfessionGuiPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class KeyBindingRegister {
    public static final KeyMapping OPEN_PROFESSION_GUI = new KeyMapping(
            "key.shadows_things.open_profession_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            "key.categories.shadows_things"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PROFESSION_GUI);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (KeyBindingRegister.OPEN_PROFESSION_GUI.consumeClick()) {
            PacketDistributor.sendToServer(new OpenProfessionGuiPacket());
        }
    }
}
