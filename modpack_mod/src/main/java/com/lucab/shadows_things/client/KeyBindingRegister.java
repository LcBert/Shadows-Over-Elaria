package com.lucab.shadows_things.client;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.client.screen.classes.ClassScreen;
import com.lucab.shadows_things.client.screen.profession.ProfessionScreen;
import com.lucab.shadows_things.rpg.classes.ClassActionExecutePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class KeyBindingRegister {
    public static final KeyMapping OPEN_CLASS_SCREEN = new KeyMapping(
            "key.shadows_things.open_class_screen",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            "key.categories.shadows_things"
    );

    public static final KeyMapping OPEN_PROFESSION_GUI = new KeyMapping(
            "key.shadows_things.open_profession_gui",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            "key.categories.shadows_things"
    );

    public static final KeyMapping CLASS_PRIMARY_ACTION = new KeyMapping(
            "key.shadows_thibgs.class_primary_action",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            "key.categories.shadows_things"
    );

    public static final KeyMapping CLASS_SECONDARY_ACTION = new KeyMapping(
            "key.shadows_thibgs.class_secondary_action",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            "key.categories.shadows_things"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CLASS_SCREEN);
        event.register(OPEN_PROFESSION_GUI);
        event.register(CLASS_PRIMARY_ACTION);
        event.register(CLASS_SECONDARY_ACTION);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        int key = event.getKey();

        if (OPEN_CLASS_SCREEN.isActiveAndMatches(InputConstants.getKey(key, event.getScanCode()))) {
            Minecraft.getInstance().setScreen(new ClassScreen());
        }
        if (OPEN_PROFESSION_GUI.isActiveAndMatches(InputConstants.getKey(key, event.getScanCode()))) {
//            PacketDistributor.sendToServer(new OpenProfessionGuiPacket());
            Minecraft.getInstance().setScreen(new ProfessionScreen());
        }
        if (CLASS_PRIMARY_ACTION.isActiveAndMatches(InputConstants.getKey(key, event.getScanCode()))) {
            PacketDistributor.sendToServer(new ClassActionExecutePacket(0));
        }
        if (CLASS_SECONDARY_ACTION.isActiveAndMatches(InputConstants.getKey(key, event.getScanCode()))) {
            PacketDistributor.sendToServer(new ClassActionExecutePacket(1));
        }
    }
}
