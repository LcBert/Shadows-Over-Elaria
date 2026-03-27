package com.lucab.shadows_things.event.farming;

import java.util.List;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DisableCropPlace {
    /**
     * Disable crop placement
     */
    @SubscribeEvent
    public static void onCropPlace(PlayerInteractEvent.RightClickBlock event) {
        Item item = event.getItemStack().getItem();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());

        List<String> disabled_crops = List.of(
                "minecraft:carrot",
                "minecraft:potato",
                "farmersdelight:onion");

        if (targetState.is(Blocks.FARMLAND) && disabled_crops.contains(item.toString())) {
            event.setCanceled(true);
        }
    }
}
