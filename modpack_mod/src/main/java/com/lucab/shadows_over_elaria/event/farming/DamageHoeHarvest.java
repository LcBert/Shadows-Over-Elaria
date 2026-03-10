package com.lucab.shadows_over_elaria.event.farming;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ShadowsOverElaria.MODID)
public class DamageHoeHarvest {
    @SubscribeEvent
    public static void onHoeHarvest(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide())
            return;

        ItemStack mainHand = event.getPlayer().getMainHandItem();
        if (event.getState().getBlock() instanceof CropBlock crop) {
            if (mainHand.is(ItemTags.HOES) && crop.isMaxAge(event.getState())) {
                mainHand.hurtAndBreak(event.getPlayer().isCreative() ? 0 : 1, event.getPlayer(),
                        EquipmentSlot.MAINHAND);
            }
        }
    }
}
