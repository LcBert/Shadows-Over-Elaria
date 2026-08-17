package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.SmelteryMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmelteryScreen extends BaseMachineScreen<SmelteryMenu> {
    public SmelteryScreen(SmelteryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, "smeltery",
                new int[]{130, 31}, new int[]{152, 47}, new int[]{20, 25}, ProgressDirection.RIGHT);
    }
}
