package com.lucab.shadows_things.client.screen.profession;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.professions.UpgradeProfessionPacket;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class ProfessionCard {
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/profession/profession_card_background.png");
    private final String PROFESSION_TRANSLATION = "gui.shadows_things.profession.name.";
    private final int BG_WIDTH = 85;
    private final int BG_HEIGHT = 61;

    private final int x, y;
    private final ProfessionHelper.Professions profession;
    private final String displayName;
    private final int level;
    private final Button actionButton;

    public ProfessionCard(int x, int y, ProfessionHelper.Professions profession) {
        this.x = x;
        this.y = y;
        this.profession = profession;
        this.displayName = Component.translatable(PROFESSION_TRANSLATION + profession.name().toLowerCase()).getString();
        this.level = ProfessionHelper.getLevel(Minecraft.getInstance().player, profession);

        this.actionButton = Button.builder(Component.literal("Upgrade"), (btn) -> {
            PacketDistributor.sendToServer(new UpgradeProfessionPacket(profession.name()));
        }).bounds(x + 5, y + 38, 75, 20).build();
    }

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);
        int nameWidth = font.width(displayName);
        int nameX = x + (BG_WIDTH / 2) - (nameWidth / 2);
        guiGraphics.drawString(font, displayName, nameX, y + 3, 0xFFFFFF, false);
        guiGraphics.drawString(font, "Level: " + level, x + 37, y + 20, 0xAAAAAA, false);
        actionButton.render(guiGraphics, mouseX, mouseY, partialTick);
        actionButton.active = ProfessionHelper.canUpgradeProfession(Minecraft.getInstance().player, profession);
    }

    public Button getActionButton() {
        return this.actionButton;
    }
}
