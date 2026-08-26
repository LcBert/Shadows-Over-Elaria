package com.lucab.shadows_things.client.screen.profession;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProfessionDetailsPanel {
    private static final ResourceLocation PANEL_BG = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/profession_details_gui.png"
    );

    public static final int PANEL_WIDTH = 144;
    public static final int PANEL_HEIGHT = 180;

    private static final int ICON_OFFSET = 3;

    private int leftPos;
    private int topPos;
    private boolean visible = false;
    private ProfessionCard boundCard;

    public void init(int leftPos, int topPos) {
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    public void show(ProfessionCard card) {
        this.boundCard = card;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
        this.boundCard = null;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void render(GuiGraphics guiGraphics) {
        if (!this.visible || this.boundCard == null) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        ProfessionHelper.Professions profession = this.boundCard.getProfession();
        ItemStack iconStack;

        Item iconItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(profession.getIconItemKey()));
        iconStack = new ItemStack(!iconItem.equals(Items.AIR) ? iconItem : Items.BARRIER);

        // Draw Panel Background
        guiGraphics.blit(PANEL_BG, this.leftPos, this.topPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 256, 256);

        // Icon Item
        int iconX = this.leftPos + ICON_OFFSET;
        int iconY = this.topPos + ICON_OFFSET;
        guiGraphics.renderItem(iconStack, iconX, iconY);

        // Panel Title
        String title = Component.translatable("gui.shadows_things.profession.name." + profession.name().toLowerCase()).getString();
        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, this.leftPos - (titleWidth / 2) + (PANEL_WIDTH / 2), this.topPos + 6, 0x000000, false);

        // Profession Level
        int currentLevel = ProfessionHelper.getLevel(mc.player, profession);
        String levelString = Component.translatable("gui.shadows_things.profession.level", currentLevel, ProfessionHelper.MAX_PROFESSION_LEVEL).getString();
        int levelStringWidth = font.width(levelString);
        guiGraphics.drawString(font, levelString, this.leftPos - (levelStringWidth / 2) + (PANEL_WIDTH / 2), this.topPos + 20, 0x000000, false);

        // Render chances/perks dynamically based on profession
        renderPerkStats(guiGraphics, font, profession, currentLevel);
    }

    private void renderPerkStats(GuiGraphics guiGraphics, Font font, ProfessionHelper.Professions profession, int level) {
        int yOffset = this.topPos + 50;

        switch (profession) {
            case BLACKSMITH -> {
                float efficiency = ProfessionHelper.getPol(ProfessionHelper.BLACKSMITH_CHANCE.repair_efficiency, level) * 100;
                float saveKit = ProfessionHelper.getPol(ProfessionHelper.BLACKSMITH_CHANCE.save_kit, level) * 100;

                guiGraphics.drawString(font, String.format("Repair Efficiency: +%.1f%%", efficiency), this.leftPos + 12, yOffset, 0x44FF44, false);
                guiGraphics.drawString(font, String.format("Save Kit Chance: %.1f%%", saveKit), this.leftPos + 12, yOffset + 14, 0x44FF44, false);
            }
            case FARMER -> {
                float extraCrop = ProfessionHelper.getPol(ProfessionHelper.FARMER_CHANCE.extra_crop_drop, level) * 100;
                float saveTool = ProfessionHelper.getPol(ProfessionHelper.FARMER_CHANCE.save_tool, level) * 100;

                guiGraphics.drawString(font, String.format("Double Drop: %.1f%%", extraCrop), this.leftPos + 12, yOffset, 0x44FF44, false);
                guiGraphics.drawString(font, String.format("Save Tool: %.1f%%", saveTool), this.leftPos + 12, yOffset + 14, 0x44FF44, false);
            }
            default -> {
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) return false;
        // Intercept clicks inside the left panel bounds
        return mouseX >= this.leftPos && mouseX <= this.leftPos + PANEL_WIDTH &&
                mouseY >= this.topPos && mouseY <= this.topPos + PANEL_HEIGHT;
    }

    public int getLeftPos() {
        return this.leftPos;
    }

    public int getTopPos() {
        return this.topPos;
    }
}