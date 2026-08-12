package com.lucab.shadows_things.client.screen.classes;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.classes.ClassSelectPacket;
import com.lucab.shadows_things.rpg.classes.ClientClassDataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClassScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, "textures/gui/screen/class/class_screen.png");

    private final int imageWidth = 176;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    private boolean hasClass = false;
    private final List<String> allClasses = new ArrayList<>();
    private String selectedClass = "";
    private int currentTab = 0;

    private ImageButton decrementTabButton, incrementTabButton;

    public ClassScreen() {
        super(Component.translatable("gui.shadows_things.class.title" +
                (!ClientClassDataHolder.hasClass() ? "_selector" : "")));
    }

    @Override
    protected void init() {
        super.init();
        this.hasClass = ClientClassDataHolder.hasClass();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        allClasses.addAll(ShadowsThings.CLASS_READER.getAllClasses().keySet());

        int buttonWidth = 23;
        int buttonHeight = 13;
        int padding = 15;
        int buttonY = this.topPos + this.imageHeight - buttonHeight - padding - 5;

        WidgetSprites backwardSprites = new WidgetSprites(
                ResourceLocation.withDefaultNamespace("widget/page_backward"),
                ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted")
        );

        WidgetSprites forwardSprites = new WidgetSprites(
                ResourceLocation.withDefaultNamespace("widget/page_forward"),
                ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted")
        );

        if (hasClass) {
            selectedClass = ClientClassDataHolder.getClassName();
            for (int i = 0; i < allClasses.size(); i++) {
                if (allClasses.get(i).equals(selectedClass)) {
                    currentTab = i;
                    break;
                }
            }
        }

        if (!hasClass) {
            if (!allClasses.isEmpty()) selectedClass = allClasses.getFirst();

            // 1. Left Button (<)
            int leftButtonX = this.leftPos + padding;
            decrementTabButton = new ImageButton(
                    leftButtonX, buttonY, buttonWidth, buttonHeight, backwardSprites,
                    button -> {
                        currentTab = Math.max(currentTab - 1, 0);
                        selectedClass = allClasses.get(currentTab);
                    }
            );

            // 2. Right Button (>)
            int rightButtonX = this.leftPos + this.imageWidth - buttonWidth - padding;
            incrementTabButton = new ImageButton(
                    rightButtonX, buttonY, buttonWidth, buttonHeight, forwardSprites,
                    button -> {
                        currentTab = Math.min(currentTab + 1, allClasses.size() - 1);
                        selectedClass = allClasses.get(currentTab);
                    }
            );

            // Select Button
            int spacingBetweenButtons = 10;
            int centerButtonX = leftButtonX + buttonWidth + spacingBetweenButtons;
            int centerButtonRightBound = rightButtonX - spacingBetweenButtons;
            int centerButtonWidth = centerButtonRightBound - centerButtonX;

            Button selectButton = Button.builder(Component.literal("Seleziona"), button -> {
                        PacketDistributor.sendToServer(new ClassSelectPacket(allClasses.get(currentTab)));
                        Minecraft.getInstance().setScreen(null);
                    })
                    .pos(centerButtonX, buttonY)
                    .size(centerButtonWidth, 20)
                    .build();

            this.addRenderableWidget(decrementTabButton);
            this.addRenderableWidget(selectButton);
            this.addRenderableWidget(incrementTabButton);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasClass) {
            decrementTabButton.visible = currentTab > 0;
            incrementTabButton.visible = currentTab < allClasses.size() - 1;
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Draw Title
        int titleWidth = this.font.width(this.title);
        int titleX = this.leftPos + (this.imageWidth - titleWidth) / 2;
        int titleY = this.topPos + 16;
        guiGraphics.drawString(this.font, this.title, titleX, titleY, 0x404040, false);

        // Draw Icon
        ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(ShadowsThings.MODID, String.format("textures/gui/screen/class/icons/%s.png", selectedClass));
        int iconX = this.leftPos + 20;
        int iconY = this.topPos + 30;
        guiGraphics.blit(iconLoc, iconX, iconY, 0, 0, 16, 16, 16, 16);

        // Draw Class Name
        String className;
        className = Component.translatable(String.format("class.shadows_things.%s.name", selectedClass)).getString();
        if (hasClass)
            className += " - " + Component.translatable("class.shadows_things.tier", ClientClassDataHolder.getTier()).getString();

        int classNameX = this.leftPos + 40;
        int classNameY = this.topPos + 34;
        guiGraphics.drawString(this.font, className, classNameX, classNameY, 0x404040, false);

        // Draw Class Description
        Component descriptionComp = Component.translatable(String.format("class.shadows_things.%s.description", selectedClass));
        int maxDescWidth = this.imageWidth - 40;
        int classDescX = this.leftPos + 20;
        int classDescY = this.topPos + 60;

        List<FormattedCharSequence> splitLines = this.font.split(descriptionComp, maxDescWidth);

        int lineHeight = this.font.lineHeight + 2;
        for (int i = 0; i < splitLines.size(); i++) {
            if ((classDescY + (i * lineHeight)) > (this.topPos + this.imageHeight - 40)) {
                break;
            }
            guiGraphics.drawString(this.font, splitLines.get(i), classDescX, classDescY + (i * lineHeight), 0x404040, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (hasClass || Minecraft.getInstance().player.isCreative()) super.onClose();
    }
}
