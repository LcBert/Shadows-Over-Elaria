package com.lucab.shadows_things.item;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class CropItem extends BlockItem {
    private String name;

    public CropItem(String name, String block) {
        super(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)),
                new Item.Properties());
        this.name = name;
    }

    @Override
    public String getDescriptionId() {
        return "item." + ShadowsThings.MODID + "." + name;
    }
}
