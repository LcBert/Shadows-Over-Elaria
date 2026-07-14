package com.lucab.shadows_things.client.screen.profession;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ProfessionCard {
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/profession/profession_card_background.png");
    private final int BG_WIDTH = 85;
    private final int BG_HEIGHT = 61;

    private final int x, y;
    private final String name;
    private final int level;
    private final Button actionButton;

    public ProfessionCard(int x, int y, String name, int level) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.level = level;
        this.actionButton = Button.builder(Component.literal("Upgrade"), (btn) -> {
        }).bounds(x + 5, y + 38, 75, 20).build();
    }

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
        int nameWidth = font.width(name);
        int nameX = x + (BG_WIDTH / 2) - (nameWidth / 2);
        guiGraphics.drawString(font, name, nameX, y + 3, 0xFFFFFF, false);
        guiGraphics.drawString(font, "Level: " + level, x + 35, y + 20, 0xAAAAAA, false);
        actionButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public Button getActionButton() {
        return this.actionButton;
    }
}
