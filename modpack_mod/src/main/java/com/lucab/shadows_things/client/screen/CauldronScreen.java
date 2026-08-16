package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.CauldronMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class CauldronScreen extends AbstractContainerScreen<CauldronMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/cauldron/cauldron_gui.png");
    private static final ResourceLocation PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/cauldron/progress_bar.png");
    private static final ResourceLocation LIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/cauldron/lit_progress.png");

    public CauldronScreen(CauldronMenu menu, Inventory playerInventory, Component title) {
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

        // Background
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        // Lit Flame
        int flameX = 80;
        int flameY = 53;
        if (this.menu.getBlockEntity().getLitTime() > 0) {
            int burnLeftProgress = getBurnLeftScaled();
            RenderSystem.setShaderTexture(0, LIT_TEXTURE);
            guiGraphics.blit(LIT_TEXTURE, relX + flameX, relY + flameY + (14 - burnLeftProgress), 0, 14 - burnLeftProgress, 14, burnLeftProgress, 14, 14);
        }

        // Progress Bar
        int progressX = 119;
        int progressY = 27;
        int processProgress = getProcessProgressScaled();
        RenderSystem.setShaderTexture(0, PROGRESS_TEXTURE);
        if (processProgress > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + progressX, relY + progressY, 0, 0, processProgress, 16, 22, 16);
        }
    }

    private int getBurnLeftScaled() {
        int litTime = this.menu.getBlockEntity().getLitTime();
        int litDuration = this.menu.getBlockEntity().getLitDuration();
        if (litDuration == 0) litDuration = 200;
        return litTime * 14 / litDuration;
    }

    private int getProcessProgressScaled() {
        int processTime = this.menu.getBlockEntity().getProcessTime();
        int totalProcessTime = this.menu.getBlockEntity().getTotalProcessTime();
        if (totalProcessTime == 0) return 0;
        return processTime * 22 / totalProcessTime;
    }
}
