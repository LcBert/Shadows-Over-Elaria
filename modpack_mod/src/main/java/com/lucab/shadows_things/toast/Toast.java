package com.lucab.shadows_things.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record Toast(
        String text,
        ChatFormatting color,
        int duration,
        Optional<ResourceLocation> soundId
) {
    public Toast(String text, ChatFormatting color, int duration) {
        this(text, color, duration, Optional.empty());
    }
}
