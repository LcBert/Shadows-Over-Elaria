package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.item.SeedsSlot;
import com.lucab.shadows_things.menus.SeedsBagMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SeedsBagScreen extends AbstractContainerScreen<SeedsBagMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/container/seeds_bag/seeds_bag_gui.png");

    public SeedsBagScreen(SeedsBagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 150;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        for (Slot slot : this.menu.slots) {
            if (slot instanceof SlotItemHandler itemHandlerSlot) {
                if (itemHandlerSlot.getItemHandler() == this.menu.getBagInventory()) {
                    if (!slot.hasItem()) {
                        int actualBagIndex = slot.getContainerSlot();
                        Item filterItem = SeedsSlot.getFilterItemForSlot(actualBagIndex);

                        if (filterItem != Items.AIR) {
                            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(filterItem), null, null, 0);
                            TextureAtlasSprite sprite = bakedModel.getParticleIcon();

                            if (sprite != null) {
                                guiGraphics.pose().pushPose();
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
                                int slotX = x + slot.x;
                                int slotY = y + slot.y;

                                guiGraphics.blit(slotX, slotY, 0, 16, 16, sprite);
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                RenderSystem.disableBlend();
                                guiGraphics.pose().popPose();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onClose() {
        Player player = Minecraft.getInstance().player;
        if (player != null) player.playNotifySound(SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0F, 0.8F);
        super.onClose();
    }
}