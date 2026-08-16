package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyMaxStackSize {
    private static final Map<Item, Integer> customMaxStackSize = new HashMap<>() {
        {
            put(Items.POTION, 64);
            put(Items.SPLASH_POTION, 64);
            put(Items.LINGERING_POTION, 64);
        }
    };

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        customMaxStackSize.forEach((item, count) -> {
            event.modify(item, builder -> builder.set(DataComponents.MAX_STACK_SIZE, count));
        });
    }
}
