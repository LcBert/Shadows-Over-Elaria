package com.lucab.shadows_things.toast;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;

public class ToastOverlay implements LayeredDraw.Layer {
    public static final ToastOverlay INSTANCE = new ToastOverlay();
    private static final int FADE_TICKS = 10;

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Toast toast = ToastClientHelper.getCurrentToast();
        if (toast == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // --- CALCOLO ALPHA (FADE IN / OUT) ---
        float alpha = getAlpha(toast);
        int alphaInt = (int) (alpha * 255);

        if (alphaInt <= 0) return; // Se è totalmente trasparente, non disegniamo nulla

        // --- PREPARAZIONE GRAFICA ---
        Font font = mc.font;
        String text = toast.text();

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int textWidth = font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 5;

        // Recuperiamo il colore RGB base
        int baseColor = toast.color().getColor() != null ? toast.color().getColor() : 0xFFFFFFFF;

        // 1. Colore del testo con Alpha animato
        int textColor = (alphaInt << 24) | (baseColor & 0xFFFFFF);

        // 2. Colore Sfondo (Nero semitrasparente max 50% di opacità -> alpha 128)
        int bgAlphaInt = (int) (alpha * 128);
        int backgroundColor = (bgAlphaInt << 24);

        // 3. Colore Bordo con Alpha animato
        int borderColor = (alphaInt << 24) | (baseColor & 0xFFFFFF);

        int paddingVertical = 5;
        int paddingHorizontal = 8;

        // --- RENDERING ---
        // Disegna Sfondo
        guiGraphics.fill(
                x - paddingHorizontal,
                y - paddingVertical,
                x + textWidth + paddingHorizontal,
                y + font.lineHeight + paddingVertical,
                backgroundColor
        );

        // Disegna Bordo
        guiGraphics.renderOutline(
                x - paddingHorizontal,
                y - paddingVertical,
                textWidth + (paddingHorizontal * 2),
                font.lineHeight + (paddingVertical * 2),
                borderColor
        );

        // Disegna Testo con Alpha applicato
        guiGraphics.drawString(font, text, x, y, textColor, true);
    }

    private static float getAlpha(Toast toast) {
        int ticks = ToastClientHelper.getTicks();
        int duration = toast.duration();
        float alpha = 1.0f;

        if (ticks < FADE_TICKS) {
            // Fade In: da 0.0 a 1.0 nei primi FADE_TICKS
            alpha = (float) ticks / FADE_TICKS;
        } else if (ticks > duration - FADE_TICKS) {
            // Fade Out: da 1.0 a 0.0 negli ultimi FADE_TICKS
            alpha = (float) (duration - ticks) / FADE_TICKS;
        }

        // Evitiamo valori negativi o superiori a 1.0
        alpha = Math.clamp(alpha, 0.0f, 1.0f);
        return alpha;
    }
}
