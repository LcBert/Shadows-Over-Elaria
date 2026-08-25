package com.lucab.shadows_things;

import com.lucab.shadows_things.content.SilverSet;
import com.lucab.shadows_things.content.block.BlockVarious;
import com.lucab.shadows_things.content.block.cauldron.CauldronRegister;
import com.lucab.shadows_things.content.block.deep_cave_portal_block.DeepCavePortalRegister;
import com.lucab.shadows_things.content.block.drying_rack.DryingRackRegister;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.repair_table.RepairTableRegister;
import com.lucab.shadows_things.content.block.sieve.SieveRegister;
import com.lucab.shadows_things.content.block.smeltery.SmelteryRegister;
import com.lucab.shadows_things.content.item.*;
import com.lucab.shadows_things.content.item.EscapeRope;
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
                    .title(Component.translatable("itemGroup." + ShadowsThings.MODID + ".main"))
                    .icon(() -> new ItemStack(Items.CARROT))
                    .displayItems((parameters, output) -> {
                        // Blocks
                        output.acceptAll(RepairTableRegister.getItems());
                        output.acceptAll(OvenRegister.getItems());
                        output.acceptAll(SmelteryRegister.getItems());
                        output.acceptAll(CauldronRegister.getItems());
                        output.acceptAll(GlassBottles.getItems());
                        output.acceptAll(DeepCavePortalRegister.getItems());
                        output.accept(EscapeRope.getItem());
                        output.acceptAll(SieveRegister.getItems());
                        output.acceptAll(DryingRackRegister.getItems());
                        output.acceptAll(SilverSet.getItems());
                        output.acceptAll(BlockVarious.getItems());

                        // Items
                        output.acceptAll(FlintTools.getItems());
                        output.acceptAll(CopperTools.getItems());
                        output.acceptAll(SeedsBagItem.getItems());
                        output.acceptAll(Crops.getItems());
                        output.acceptAll(ItemVarious.getItems());
                        output.acceptAll(SmelteryDie.getItems());
                        output.acceptAll(Plates.getItems());
                        output.acceptAll(RepairKits.getItems());
                        output.acceptAll(Rods.getItems());
                        output.acceptAll(Hilts.getItems());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOWS_GEM_TAB = CREATIVE_TABS
            .register("shadows_gem_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ShadowsThings.MODID + ".gems"))
                    .icon(() -> GemItem.getGems().getFirst())
                    .displayItems((parameters, output) -> {
                        output.acceptAll(GemItem.getGems());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
