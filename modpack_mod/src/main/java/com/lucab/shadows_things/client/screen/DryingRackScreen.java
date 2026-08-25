package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.DryingRackMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DryingRackScreen extends AbstractContainerScreen<DryingRackMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/drying_rack/drying_rack_gui.png");
    private static final ResourceLocation PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/drying_rack/progress.png");

    public DryingRackScreen(DryingRackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 90;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        int processX = 45;
        int processY = 38;
        int processRowCount = 5;
        int processRowWidth = 16;
        int processRowSpacing = 2;

        for (int row = 0; row < processRowCount; row++) {
            int progress = getProcessScaled(row);
            if (progress > 0) {
                int xPos = relX + processX + row * (processRowWidth + processRowSpacing);
                int yPos = relY + processY;
                guiGraphics.blit(PROGRESS_TEXTURE, xPos, yPos, 0, 0, 16, progress, 16, 24);
            }
        }
    }

    private int getProcessScaled(int dataIndex) {
        int[] process = this.menu.getBlockEntity().getProcessTime(dataIndex);
        int processTime = process[0];
        int totalProcessTime = process[1];
        if (totalProcessTime == 0) return 0;
        return processTime * 24 / totalProcessTime;
    }
}
