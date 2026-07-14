package com.lucab.shadows_things.client.screen;

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
    private static final ResourceLocation PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/oven/burn_progress.png");
    private static final ResourceLocation LIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/oven/lit_progress.png");

    public OvenScreen(OvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Imposta le dimensioni della texture di base della GUI
        this.imageWidth = 176;
        this.imageHeight = 184;

        // Sposta i titoli testuali Vanilla per non sovrapporli agli slot custom
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 90; // Spostato appena sopra l'inventario del player (Y=84)
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Renderizza lo sfondo scuro trasparente classico dietro la GUI e i tooltip degli item
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

        // 1. Sfondo principale
        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        // 2. Fiamma del carburante (Sopra lo slot Fuel, Y sale mentre si consuma)
        if (this.menu.getBlockEntity().getContainerData().get(0) > 0) {
            int burnLeftProgress = getBurnLeftScaled();
            RenderSystem.setShaderTexture(0, LIT_TEXTURE);
            guiGraphics.blit(LIT_TEXTURE, relX + 118, relY + 23 + (14 - burnLeftProgress), 0, 14 - burnLeftProgress, 14, burnLeftProgress, 14, 14);
        }

        // 3. Frecce di progresso verticali
        int cookProgress0 = getCookProgressScaled(0, 24);
        int cookProgress1 = getCookProgressScaled(1, 24);
        int cookProgress2 = getCookProgressScaled(2, 24);

        RenderSystem.setShaderTexture(0, PROGRESS_TEXTURE);

        // Freccia 0
        if (cookProgress0 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 43, relY + 35, 0, 0, 16, cookProgress0, 16, 24);
        }
        // Freccia 1
        if (cookProgress1 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 65, relY + 35, 0, 0, 16, cookProgress1, 16, 24);
        }
        // Freccia 2
        if (cookProgress2 > 0) {
            guiGraphics.blit(PROGRESS_TEXTURE, relX + 87, relY + 35, 0, 0, 16, cookProgress2, 16, 24);
        }
    }

    // Calcola quanti pixel della fiamma renderizzare (Altezza standard sprite fiamma vanilla = 14px)
    private int getBurnLeftScaled() {
        int litTime = this.menu.getBlockEntity().getContainerData().get(0);
        int litDuration = this.menu.getBlockEntity().getContainerData().get(1);
        if (litDuration == 0) litDuration = 200; // Fallback di sicurezza
        return litTime * 14 / litDuration;
    }

    // Calcola quanti pixel riempire della freccia di cottura verticale (Altezza standard freccia giù = 14px)
    private int getCookProgressScaled(int dataIndex, int maxPixels) {
        int cookTime = this.menu.getBlockEntity().getContainerData().get(dataIndex + 2);
        int totalCookTime = this.menu.getBlockEntity().getContainerData().get(dataIndex + 5); // Indice 5 = totale ricetta
        if (totalCookTime == 0) return 0;
        return cookTime * maxPixels / totalCookTime;
    }
}
