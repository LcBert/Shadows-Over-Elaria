package com.lucab.shadows_over_elaria;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.lucab.shadows_over_elaria.block.BlocksRegister;
import com.lucab.shadows_over_elaria.item.Crops;
import com.lucab.shadows_over_elaria.item.Plates;
import com.lucab.shadows_over_elaria.item.RepairKits;
import com.lucab.shadows_over_elaria.item.Rods;

import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ShadowsOverElaria.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOWS_TAB = CREATIVE_TABS
            .register("shadows_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.shadows_over_elaria"))
                    .icon(() -> new ItemStack(Items.CARROT))
                    .displayItems((parameters, output) -> {
                        output.acceptAll(Crops.getItems());
                        output.acceptAll(Plates.getItems());
                        output.acceptAll(Rods.getItems());
                        output.acceptAll(RepairKits.getItems());
                        output.acceptAll(BlocksRegister.getItems());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
