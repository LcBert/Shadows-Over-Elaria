package com.lucab.shadows_things;

import com.lucab.shadows_things.item.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.lucab.shadows_things.block.BlocksRegister;

import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ShadowsThings.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOWS_TAB = CREATIVE_TABS
            .register("shadows_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ShadowsThings.MODID))
                    .icon(() -> new ItemStack(Items.CARROT))
                    .displayItems((parameters, output) -> {
                        output.acceptAll(BlocksRegister.getItems());
                        output.acceptAll(FlintTools.getItems());
                        output.acceptAll(CopperTools.getItems());
                        output.acceptAll(Crops.getItems());
                        output.acceptAll(Plates.getItems());
                        output.acceptAll(Rods.getItems());
                        output.acceptAll(Hilts.getItems());
                        output.acceptAll(Various.getItems());
                        output.acceptAll(RepairKits.getItems());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
