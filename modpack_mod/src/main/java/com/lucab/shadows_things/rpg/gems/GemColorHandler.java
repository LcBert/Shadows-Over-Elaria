package com.lucab.shadows_things.rpg.gems;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.item.GemItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID, value = Dist.CLIENT)
public class GemColorHandler {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return 0xFFFFFFFF;
            }

            if (tintIndex != 0) {
                return 0xFFFFFFFF;
            }

            GemData data = stack.get(SocketRegistries.GEM_DATA_COMPONENT.get());
            if (data == null) {
                return 0xFF808080;
            }

            return GemDataReader.get(data.gemId())
                    .map(def -> calculateTierColor(def.color(), data.rarity()))
                    .orElse(0xFFFFFFFF);
        }, GemItem.GEM_ITEM.get());
    }

    private static int calculateTierColor(int baseColor, int tier) {
        float factor = 0.6f + (tier * 0.08f);

        int r = (int) (((baseColor >> 16) & 0xFF) * factor);
        int g = (int) (((baseColor >> 8) & 0xFF) * factor);
        int b = (int) ((baseColor & 0xFF) * factor);

        return (0xFF << 24) | (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255);
    }
}
