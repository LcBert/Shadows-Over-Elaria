package com.lucab.shadows_over_elaria.event.farming;

import java.util.List;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ShadowsOverElaria.MODID)
public class DisableCropPlace {
    /**
     * Disable crop placement
     */
    @SubscribeEvent
    public static void onCropPlace(PlayerInteractEvent.RightClickBlock event) {
        Item item = event.getItemStack().getItem();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());

        List<Item> disabled_crops = List.of(
                Items.CARROT,
                Items.POTATO);

        if (targetState.is(Blocks.FARMLAND) && disabled_crops.contains(item)) {
            event.setCanceled(true);
        }
    }
}
