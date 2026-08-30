package com.lucab.shadows_things.client.overlay;

import com.lucab.shadows_things.rpg.classes.ClassManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class ClassExperienceBarOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation BAR_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/experience_bar_background.png");
    private static final ResourceLocation BAR_PROGRESS = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/experience_bar_progress.png");

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private static final int TEXTURE_WIDTH = 182;
    private static final int TEXTURE_HEIGHT = 5;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = screenHeight - 32 + 3;

        float progress = ClassManager.getExperienceProgress(mc.player);
        int filledWidth = (int) (progress * (BAR_WIDTH + 1));

        guiGraphics.blit(BAR_BACKGROUND, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (filledWidth > 0) {
            guiGraphics.blit(BAR_PROGRESS, x, y, 0, 5, filledWidth, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        int currentLevel = ClassManager.getTier(mc.player);
        if (currentLevel > 0) {
            String text = String.valueOf(currentLevel);
            int textX = (screenWidth - mc.font.width(text)) / 2;
            int textY = y - 6;

            guiGraphics.drawString(mc.font, text, textX, textY, 0x60FF20, true);
        }
    }
}
