package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.BaseMachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BaseMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {
    private final ResourceLocation BACKGROUND;
    private final ResourceLocation PROGRESS_TEXTURE;
    private final ResourceLocation LIT_TEXTURE;

    private final int[] progressCords;
    private final int[] fuelCords;
    private final int[] progressSize;
    private final ProgressDirection progressDirection;

    public static enum ProgressDirection {UP, DOWN, LEFT, RIGHT;}

    public BaseMachineScreen(T menu, Inventory playerInventory, Component title, String name,
                             int[] progressCords, int[] fuelCords, int[] progressSize, ProgressDirection progressDirection) {
        super(menu, playerInventory, title);

        this.BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID,
                String.format("textures/gui/container/%s/%s_gui.png", name, name));

        this.PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID,
                String.format("textures/gui/container/%s/progress.png", name));

        this.LIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID,
                String.format("textures/gui/container/%s/lit.png", name));

        this.imageWidth = 176;
        this.imageHeight = 184;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 90;

        this.progressCords = progressCords;
        this.fuelCords = fuelCords;
        this.progressSize = progressSize;
        this.progressDirection = progressDirection;
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

        // Progress Bar
        int[] progressData = getProcessProgressScaled();
        int uOffset = progressData[0];
        int vOffset = progressData[1];
        int renderWidth = progressData[2];
        int renderHeight = progressData[3];

        if (renderWidth > 0 && renderHeight > 0) {
            guiGraphics.blit(
                    PROGRESS_TEXTURE,
                    relX + progressCords[0] + uOffset,
                    relY + progressCords[1] + vOffset,
                    uOffset,
                    vOffset,
                    renderWidth,
                    renderHeight,
                    progressSize[0],
                    progressSize[1]
            );
        }

        // Lit Flame
        if (this.menu.getLitTime() > 0) {
            int burnLeftProgress = getBurnLeftScaled();
            RenderSystem.setShaderTexture(0, LIT_TEXTURE);
            guiGraphics.blit(LIT_TEXTURE, relX + fuelCords[0], relY + fuelCords[1] + (14 - burnLeftProgress), 0, 14 - burnLeftProgress, 14, burnLeftProgress, 14, 14);
        }
    }

    private int getBurnLeftScaled() {
        int litTime = this.menu.getLitTime();
        int litDuration = this.menu.getLitDuration();
        if (litDuration == 0) litDuration = 200;
        return litTime * 14 / litDuration;
    }

    private int[] getProcessProgressScaled() {
        int processTime = this.menu.getProcessTime();
        int totalProcessTime = this.menu.getTotalProcessTime();
        if (totalProcessTime == 0 || processTime == 0) return new int[]{0, 0, 0, 0};

        int maxWidth = progressSize[0];
        int maxHeight = progressSize[1];

        int currentWidth = (processTime * maxWidth) / totalProcessTime;
        int currentHeight = (processTime * maxHeight) / totalProcessTime;

        return switch (this.progressDirection) {
            case RIGHT -> new int[]{0, 0, currentWidth, maxHeight};
            case LEFT -> new int[]{maxWidth - currentWidth, 0, currentWidth, maxHeight};
            case DOWN -> new int[]{0, 0, maxWidth, currentHeight};
            case UP -> new int[]{0, maxHeight - currentHeight, maxWidth, currentHeight};
        };
    }
}
