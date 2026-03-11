package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class TreeBark {
    @SubscribeEvent
    public static void onPlayerInteract(LeftClickBlock event) {
        if (event.getEntity().isCreative())
            return;

        var state = event.getLevel().getBlockState(event.getPos());
        boolean isLog = state.is(BlockTags.LOGS);

        if (isLog && !event.getItemStack().is(ItemTags.create(ResourceLocation.parse("minecraft:axes"))))
            event.setCanceled(true);
    }
}
