package com.lucab.shadows_over_elaria.item;

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
        return "item.shadows_over_elaria." + name;
    }
}
