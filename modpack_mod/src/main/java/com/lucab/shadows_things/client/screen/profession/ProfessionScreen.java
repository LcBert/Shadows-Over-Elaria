package com.lucab.shadows_things.client.screen.profession;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import com.lucab.shadows_things.rpg.professions.UpgradeProfessionPacket;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ProfessionScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/profession_gui.png"
    );
    private static final ResourceLocation XP_BAR_BG = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/xp_progress_background.png"
    );
    private static final ResourceLocation XP_BAR_FILL = ResourceLocation.fromNamespaceAndPath(
            ShadowsThings.MODID, "textures/gui/screen/profession/xp_progress_filled.png"
    );

    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private int leftPos;
    private int topPos;

    private static final int IMAGE_WIDTH = 144;
    private static final int IMAGE_HEIGHT = 180;

    private static final int BAR_WIDTH = 130;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_OFFSET_Y = 30;
    private static final int XP_TEXT_OFFSET_Y = 20;
    private static final int POINTS_TEXT_OFFSET_Y = 40;

    private static final int CARD_START_X = 7;
    private static final int CARD_START_Y = 50;
    private static final int CARD_Y_SPACING = 1;
    private static final int VIEWPORT_WIDTH = 115;
    private static final int VIEWPORT_HEIGHT = 123;

    private static final int SCROLL_TRACK_X = 125;
    private static final int SCROLL_TRACK_Y = 51;
    private static final int SCROLL_TRACK_HEIGHT = 121;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;

    // Spacing between the main window and side panel
    private static final int PANEL_GAP = 4;

    private double scrollAmount = 0.0;
    private boolean isScrolling = false;

    private final List<ProfessionCard> professionCards = new ArrayList<>();
    @Nullable
    private ProfessionCard selectedCard = null;

    private final ProfessionDetailsPanel detailsPanel = new ProfessionDetailsPanel();
    private Button upgradeButton;

    public ProfessionScreen() {
        super(Component.translatable("gui.shadows_things.profession.title"));
    }

    @Override
    protected void init() {
        super.init();
        recalculateLayoutPositions();

        this.clearWidgets();
        setupProfessionCards();

        // Upgrade button positioned inside the right details panel
        this.upgradeButton = addRenderableWidget(Button.builder(Component.translatable("gui.shadows_things.profession.button.levelup"), btn -> {
            if (this.selectedCard != null) {
                ProfessionHelper.Professions prof = this.selectedCard.getProfession();
                if (ProfessionHelper.canUpgradeProfession(this.minecraft.player, prof)) {
                    PacketDistributor.sendToServer(new UpgradeProfessionPacket(prof.name()));

                    this.minecraft.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F)
                    );

                    updateWidgetPositions();
                }
            }
        }).bounds(0, 0, 70, 20).build());

        updateWidgetPositions();
    }

    /**
     * Shifts main window to the left and positions the details panel to its right.
     */
    private void recalculateLayoutPositions() {
        int xOffset = this.detailsPanel.isVisible() ? (ProfessionDetailsPanel.PANEL_WIDTH + PANEL_GAP) / 2 : 0;
        this.leftPos = ((this.width - IMAGE_WIDTH) / 2) - xOffset;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;

        int detailsLeft = this.leftPos + IMAGE_WIDTH + PANEL_GAP;
        this.detailsPanel.init(detailsLeft, this.topPos);
    }

    private void updateWidgetPositions() {
        if (this.upgradeButton != null) {
            this.upgradeButton.visible = this.detailsPanel.isVisible();
            this.upgradeButton.active = this.selectedCard != null &&
                    ProfessionHelper.canUpgradeProfession(this.minecraft.player, this.selectedCard.getProfession());
            this.upgradeButton.setX(this.detailsPanel.getLeftPos() + (ProfessionDetailsPanel.PANEL_WIDTH - 70) / 2);
            this.upgradeButton.setY(this.detailsPanel.getTopPos() + 150);
        }
    }

    private void setupProfessionCards() {
        ProfessionHelper.Professions previousSelectedProfession = this.selectedCard != null ? this.selectedCard.getProfession() : null;
        this.professionCards.clear();
        this.selectedCard = null;

        ProfessionHelper.Professions[] professions = ProfessionHelper.Professions.values();

        for (int i = 0; i < professions.length; i++) {
            int posX = this.leftPos + CARD_START_X;
            int posY = this.topPos + CARD_START_Y + i * (ProfessionCard.BG_HEIGHT + CARD_Y_SPACING);
            ProfessionCard card = new ProfessionCard(posX, posY, professions[i]);

            if (professions[i] == previousSelectedProfession) {
                card.setSelected(true);
                this.selectedCard = card;
                this.detailsPanel.show(card);
            }

            this.professionCards.add(card);
        }
    }

    private void updateCardPositions() {
        for (int i = 0; i < this.professionCards.size(); i++) {
            int posX = this.leftPos + CARD_START_X;
            int posY = this.topPos + CARD_START_Y + i * (ProfessionCard.BG_HEIGHT + CARD_Y_SPACING);
            this.professionCards.get(i).updatePosition(posX, posY);
        }
    }

    private int getMaxScroll() {
        int totalContentHeight = this.professionCards.size() * (ProfessionCard.BG_HEIGHT + CARD_Y_SPACING) - CARD_Y_SPACING;
        return Math.max(0, totalContentHeight - VIEWPORT_HEIGHT);
    }

    private boolean canScroll() {
        return getMaxScroll() > 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = this.getMinecraft().font;

        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 1. Render Right Details Panel (if active)
        this.detailsPanel.render(guiGraphics);

        // 2. Main GUI Background
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 3. Header Texts
        int headerWidth = font.width(this.title.getString());
        guiGraphics.drawString(font, this.title.getString(), this.leftPos - (headerWidth / 2) + (IMAGE_WIDTH / 2), this.topPos + 6, 0xFF000000, false);
        renderXpBar(guiGraphics);
        renderPoints(guiGraphics);

        // 4. Scrollbar
        renderScrollBar(guiGraphics);

        // 5. Cards Viewport with Scissor Clipping
        int viewportMinX = this.leftPos + CARD_START_X;
        int viewportMinY = this.topPos + CARD_START_Y;
        int viewportMaxX = viewportMinX + VIEWPORT_WIDTH;
        int viewportMaxY = viewportMinY + VIEWPORT_HEIGHT;

        guiGraphics.enableScissor(viewportMinX, viewportMinY, viewportMaxX, viewportMaxY);

        int currentScroll = (int) this.scrollAmount;
        for (ProfessionCard card : this.professionCards) {
            int cardRenderY = card.getBasePosY() - currentScroll;
            if (cardRenderY + ProfessionCard.BG_HEIGHT >= viewportMinY && cardRenderY <= viewportMaxY) {
                card.render(guiGraphics, currentScroll);
            }
        }

        guiGraphics.disableScissor();

        // 6. Render Screen Widgets (Buttons, etc.)
        for (GuiEventListener listener : this.children()) {
            if (listener instanceof Renderable renderable) {
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        int thumbX = this.leftPos + SCROLL_TRACK_X;
        int thumbY;

        if (canScroll()) {
            int maxScroll = getMaxScroll();
            int maxThumbTravel = SCROLL_TRACK_HEIGHT - SCROLLER_HEIGHT;
            int scrollerYOffset = (int) ((this.scrollAmount / maxScroll) * maxThumbTravel);
            thumbY = this.topPos + SCROLL_TRACK_Y + scrollerYOffset;

            guiGraphics.blitSprite(SCROLLER_SPRITE, thumbX, thumbY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        } else {
            thumbY = this.topPos + SCROLL_TRACK_Y;
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, thumbX, thumbY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (canScroll()) {
            int maxScroll = getMaxScroll();
            this.scrollAmount = Mth.clamp(this.scrollAmount - scrollY * 14.0, 0.0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check click inside Right Details Panel
            if (this.detailsPanel.mouseClicked(mouseX, mouseY, button)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int trackMinX = this.leftPos + SCROLL_TRACK_X;
            int trackMinY = this.topPos + SCROLL_TRACK_Y;
            int trackMaxX = trackMinX + SCROLLER_WIDTH;
            int trackMaxY = trackMinY + SCROLL_TRACK_HEIGHT;

            // Click on scrollbar
            if (mouseX >= trackMinX && mouseX <= trackMaxX && mouseY >= trackMinY && mouseY <= trackMaxY) {
                this.isScrolling = canScroll();
                updateScrollFromMouse(mouseY);
                return true;
            }

            // Click inside Cards Viewport
            int viewportMinX = this.leftPos + CARD_START_X;
            int viewportMinY = this.topPos + CARD_START_Y;
            if (mouseX >= viewportMinX && mouseX <= viewportMinX + VIEWPORT_WIDTH &&
                    mouseY >= viewportMinY && mouseY <= viewportMinY + VIEWPORT_HEIGHT) {

                int currentScroll = (int) this.scrollAmount;
                for (ProfessionCard card : this.professionCards) {
                    if (card.isMouseOver(mouseX, mouseY, currentScroll)) {
                        toggleCardSelection(card);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleCardSelection(ProfessionCard targetCard) {
        if (this.selectedCard == targetCard) {
            this.selectedCard.setSelected(false);
            this.selectedCard = null;
            this.detailsPanel.hide();
        } else {
            if (this.selectedCard != null) {
                this.selectedCard.setSelected(false);
            }
            this.selectedCard = targetCard;
            this.selectedCard.setSelected(true);
            this.detailsPanel.show(targetCard);
        }

        this.getMinecraft().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );

        recalculateLayoutPositions();
        updateCardPositions();
        updateWidgetPositions();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isScrolling && button == 0) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isScrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollFromMouse(double mouseY) {
        if (!canScroll()) {
            this.scrollAmount = 0.0;
            return;
        }

        int maxScroll = getMaxScroll();
        int trackStartY = this.topPos + SCROLL_TRACK_Y;
        int maxTravel = SCROLL_TRACK_HEIGHT - SCROLLER_HEIGHT;

        double progress = (mouseY - trackStartY - (SCROLLER_HEIGHT / 2.0)) / (double) maxTravel;
        this.scrollAmount = Mth.clamp(progress * maxScroll, 0.0, maxScroll);
    }

    private void renderXpBar(GuiGraphics guiGraphics) {
        Player player = this.getMinecraft().player;
        int experience = ProfessionHelper.getExperience(player);
        int requiredExperience = ProfessionHelper.getExperienceRequired(player);

        float progress = (requiredExperience > 0) ? (float) experience / requiredExperience : 0;
        progress = Math.min(progress, 1.0f);
        int progressWidth = (int) (progress * BAR_WIDTH);

        guiGraphics.blit(XP_BAR_BG, this.leftPos + (IMAGE_WIDTH / 2) - (BAR_WIDTH / 2), this.topPos + BAR_OFFSET_Y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        if (progressWidth > 0) {
            guiGraphics.blit(XP_BAR_FILL, this.leftPos + (IMAGE_WIDTH / 2) - (BAR_WIDTH / 2), this.topPos + BAR_OFFSET_Y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        String progressText = experience + " / " + requiredExperience;
        int textWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText, this.leftPos + (IMAGE_WIDTH / 2) - (textWidth / 2), this.topPos + XP_TEXT_OFFSET_Y, 0x000000, false);
    }

    private void renderPoints(GuiGraphics guiGraphics) {
        int points = ProfessionHelper.getPoints(this.getMinecraft().player);
        String pointsText = Component.translatable("gui.shadows_things.profession.available_points").getString() + ": " + points;
        int textWidth = this.font.width(pointsText);
        guiGraphics.drawString(this.font, pointsText, this.leftPos + (IMAGE_WIDTH / 2) - (textWidth / 2), this.topPos + POINTS_TEXT_OFFSET_Y, 0x000000, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}