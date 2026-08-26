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

public class ProfessionCard {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/profession_card_background.png"
    );
    private static final ResourceLocation BACKGROUND_SELECTED = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/profession_card_background_selected.png"
    );
    private static final String PROFESSION_TRANSLATION = "gui.shadows_things.profession.name.";
    public static final int BG_WIDTH = 115;
    public static final int BG_HEIGHT = 30;

    // Icon position offsets
    private static final int ICON_OFFSET_X = 3;
    private static final int ICON_SIZE = 16;

    private int x;
    private int basePosY;
    private final ProfessionHelper.Professions profession;
    private final String displayName;
    private ItemStack iconStack;
    private boolean selected = false;

    public ProfessionCard(int x, int basePosY, ProfessionHelper.Professions profession) {
        this.x = x;
        this.basePosY = basePosY;
        this.profession = profession;
        this.displayName = Component.translatable(PROFESSION_TRANSLATION + profession.name().toLowerCase()).getString();

        Item iconItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(profession.getIconItemKey()));
        this.iconStack = new ItemStack(!iconItem.equals(Items.AIR) ? iconItem : Items.BARRIER);
    }

    public void updatePosition(int x, int basePosY) {
        this.x = x;
        this.basePosY = basePosY;
    }

    public void render(GuiGraphics guiGraphics, int scrollOffset) {
        int currentY = this.basePosY - scrollOffset;
        Font font = Minecraft.getInstance().font;

        int currentLevel = ProfessionHelper.getLevel(Minecraft.getInstance().player, this.profession);

        ResourceLocation bgTexture = this.selected ? BACKGROUND_SELECTED : BACKGROUND;
        guiGraphics.blit(bgTexture, this.x, currentY, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        int iconX = this.x + ICON_OFFSET_X;
        int iconY = currentY + (BG_HEIGHT - ICON_SIZE) / 2;
        guiGraphics.renderItem(this.iconStack, iconX, iconY);

        int textHeight = currentY - (font.lineHeight / 2) + (BG_HEIGHT / 2) + 1;
        guiGraphics.drawString(font, this.displayName, this.x + 25, textHeight - 5, 0xFFFFFF, false);
        guiGraphics.drawString(font, "Level: " + currentLevel, this.x + 25, textHeight + 5, 0xAAAAAA, false);
    }

    public boolean isMouseOver(double mouseX, double mouseY, int scrollOffset) {
        int currentY = this.basePosY - scrollOffset;
        return mouseX >= this.x && mouseX <= this.x + BG_WIDTH && mouseY >= currentY && mouseY <= currentY + BG_HEIGHT;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public int getBasePosY() {
        return this.basePosY;
    }

    public ProfessionHelper.Professions getProfession() {
        return this.profession;
    }
}