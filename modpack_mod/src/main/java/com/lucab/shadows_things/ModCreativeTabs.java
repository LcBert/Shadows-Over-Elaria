package com.lucab.shadows_things;

import com.lucab.shadows_things.content.SilverSet;
import com.lucab.shadows_things.content.block.deep_cave_portal_block.DeepCavePortalRegister;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.repair_table.RepairTableRegister;
import com.lucab.shadows_things.content.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ShadowsThings.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOWS_TAB = CREATIVE_TABS
            .register("shadows_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ShadowsThings.MODID))
                    .icon(() -> new ItemStack(Items.CARROT))
                    .displayItems((parameters, output) -> {
                        // Blocks
                        output.acceptAll(RepairTableRegister.getItems());
                        output.acceptAll(OvenRegister.getItems());
                        output.acceptAll(DeepCavePortalRegister.getItems());
                        output.acceptAll(SilverSet.getItems());

                        // Items
                        output.acceptAll(FlintTools.getItems());
                        output.acceptAll(CopperTools.getItems());
                        output.acceptAll(SeedsBagItem.getItems());
                        output.acceptAll(Crops.getItems());
                        output.acceptAll(ItemVarious.getItems());
                        output.acceptAll(Plates.getItems());
                        output.acceptAll(RepairKits.getItems());
                        output.acceptAll(Rods.getItems());
                        output.acceptAll(Hilts.getItems());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
