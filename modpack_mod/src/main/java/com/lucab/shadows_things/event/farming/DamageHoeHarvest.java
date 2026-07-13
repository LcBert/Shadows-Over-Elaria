package com.lucab.shadows_things.event.farming;

import com.lucab.shadows_things.ShadowsThings;

import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class DamageHoeHarvest {
    @SubscribeEvent
    public static void onHoeHarvest(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getMainHandItem();

        if (event.getState().getBlock() instanceof CropBlock crop) {
            if (mainHand.is(ItemTags.HOES) && crop.isMaxAge(event.getState())) {
                int farmerLevel = ProfessionHelper.getLevel(player, ProfessionHelper.Professions.FARMER);

                double saveToolChance = ProfessionHelper.getPol(ProfessionHelper.FARMER_CHANCE.save_tool, farmerLevel);

                if (player.level().random.nextDouble() >= saveToolChance)
                    mainHand.hurtAndBreak(event.getPlayer().isCreative() ? 0 : 1, event.getPlayer(), EquipmentSlot.MAINHAND);
            }
        }
    }
}
