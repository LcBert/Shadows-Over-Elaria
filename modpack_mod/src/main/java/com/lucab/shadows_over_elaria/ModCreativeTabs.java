package com.lucab.shadows_over_elaria;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ShadowsOverElaria.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOWS_TAB = CREATIVE_TABS
            .register("shadows_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.shadows_over_elaria"))
                    .icon(() -> new ItemStack(Items.CARROT)) // Change to a custom item if desired
                    .displayItems((parameters, output) -> {
                        // Add your mod's items here
                        output.accept(com.lucab.shadows_over_elaria.item.Crops.CARROT_SEEDS.get());
                        output.accept(com.lucab.shadows_over_elaria.item.Crops.POTATO_SEEDS.get());
                    })
                    .build());

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
