package com.lucab.shadows_things.rpg.gems;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.gem_set.GemItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class GemColorHandler {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            int activeLayer = getGemIndex(stack);
            if (tintIndex == activeLayer) return 0xFFFFFFFF;
            return 0x00000000;
        }, GemItem.GEM_ITEM.get());
    }

    private static int getGemIndex(ItemStack stack) {
        GemData data = stack.get(SocketRegistries.GEM_DATA.get());
        if (data != null) {
            String gemPath = data.gemId().getPath();
            if (gemPath.equals("ruby")) return 1;
            if (gemPath.equals("sapphire")) return 2;
        }
        return 0;
    }
}
