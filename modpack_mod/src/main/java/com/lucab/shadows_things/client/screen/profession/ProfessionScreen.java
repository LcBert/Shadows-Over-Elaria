package com.lucab.shadows_things.client.screen.profession;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.ProfessionMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProfessionScreen extends AbstractContainerScreen<ProfessionMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/profession/profession_gui.png");
    private static final ResourceLocation XP_BAR_BG = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/profession/xp_progress_background.png");
    private static final ResourceLocation XP_BAR_FILL = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/profession/xp_progress_filled.png");

    private static final int BAR_WIDTH = 163;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_OFFSET_Y = 35;
    private static final int TEXT_OFFSET_Y = 25;

    private final List<ProfessionCard> professionCards = new ArrayList<>();

    public ProfessionScreen(ProfessionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 190;
        this.imageHeight = 256;

        this.inventoryLabelX = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        professionCards.clear();
        professionCards.add(new ProfessionCard(leftPos + 5, topPos + 50, "Blacksmith", menu.getBlacksmithLevel()));
        professionCards.add(new ProfessionCard(leftPos + 100, topPos + 50, "Farmer", menu.getFarmerLevel()));

        for (ProfessionCard card : professionCards) {
            this.addRenderableWidget(card.getActionButton());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);

        renderXpBar(guiGraphics, x, y);

        for (ProfessionCard card : professionCards) {
            card.render(guiGraphics, this.font, mouseX, mouseY, partialTick);
        }
    }

    private void renderXpBar(GuiGraphics guiGraphics, int x, int y) {
        int experience = this.menu.getExperience();
        int requiredExperience = this.menu.getExperienceRequired();

        float progress = (requiredExperience > 0) ? (float) experience / requiredExperience : 0;
        int progressWidth = (int) (progress * BAR_WIDTH);

        guiGraphics.blit(XP_BAR_BG, x + (imageWidth / 2) - (BAR_WIDTH / 2), y + BAR_OFFSET_Y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        if (progressWidth > 0) {
            guiGraphics.blit(XP_BAR_FILL, x + (imageWidth / 2) - (BAR_WIDTH / 2), y + BAR_OFFSET_Y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        String progressText = experience + " / " + requiredExperience;
        int textWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText, x + 15 + (BAR_WIDTH / 2) - (textWidth / 2), y + TEXT_OFFSET_Y, 0x000000, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
