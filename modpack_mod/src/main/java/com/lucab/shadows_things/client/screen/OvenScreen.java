package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.content.block.oven.OvenBlockEntity;
import com.lucab.shadows_things.menus.OvenMenu;
import com.lucab.shadows_things.ShadowsThings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OvenScreen extends AbstractContainerScreen<OvenMenu> {
    // Percorso della texture del background (assets/tuo_modid/textures/gui/container/oven_gui.png)
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/oven/oven_gui.png");
    private static final ResourceLocation PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/oven/progress_bar.png");
    private static final ResourceLocation LIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/oven/lit_progress.png");

    public OvenScreen(OvenMenu menu, Inventory playerInventory, Component title) {
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

        if (this.menu.getBlockEntity().getContainerData().get(0) > 0) {
            int burnLeftProgress = getBurnLeftScaled();
            RenderSystem.setShaderTexture(0, LIT_TEXTURE);
            guiGraphics.blit(LIT_TEXTURE, relX + 118, relY + 23 + (14 - burnLeftProgress), 0, 14 - burnLeftProgress, 14, burnLeftProgress, 14, 14);
        }

        int cookProgress0 = getCookProgressScaled(0);
        int cookProgress1 = getCookProgressScaled(1);
        int cookProgress2 = getCookProgressScaled(2);

        RenderSystem.setShaderTexture(0, PROGRESS_TEXTURE);

        if (cookProgress0 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 43, relY + 35, 0, 0, 16, cookProgress0, 16, 24);
        }
        if (cookProgress1 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 65, relY + 35, 0, 0, 16, cookProgress1, 16, 24);
        }
        if (cookProgress2 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 87, relY + 35, 0, 0, 16, cookProgress2, 16, 24);
        }
    }

    private int getBurnLeftScaled() {
        int litTime = this.menu.getBlockEntity().getContainerData().get(0);
        int litDuration = this.menu.getBlockEntity().getContainerData().get(1);
        if (litDuration == 0) litDuration = 200; // Fallback di sicurezza
        return litTime * 14 / litDuration;
    }

    private int getCookProgressScaled(int dataIndex) {
        int cookTime = this.menu.getBlockEntity().getContainerData().get(dataIndex + 2);
        int totalCookTime = this.menu.getBlockEntity().getContainerData().get(dataIndex + 5);
        if (totalCookTime == 0) return 0;
        return cookTime * 24 / totalCookTime;
    }
}
