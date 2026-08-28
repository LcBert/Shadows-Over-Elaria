package com.lucab.shadows_things.content;

import com.lucab.shadows_things.content.block.BlockVarious;
import com.lucab.shadows_things.content.block.cauldron.CauldronRegister;
import com.lucab.shadows_things.content.block.deep_cave_portal_block.DeepCavePortalRegister;
import com.lucab.shadows_things.content.block.drying_rack.DryingRackRegister;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.repair_table.RepairTableRegister;
import com.lucab.shadows_things.content.block.resonant.resonant_altar.ResonantAltarRegistry;
import com.lucab.shadows_things.content.block.resonant.resonant_pedestal.ResonantPedestalRegistry;
import com.lucab.shadows_things.content.block.sieve.SieveRegister;
import com.lucab.shadows_things.content.block.smeltery.SmelteryRegister;
import com.lucab.shadows_things.content.item.*;
import com.lucab.shadows_things.content.item.EscapeRope;

public class ContentRegister {
    public static void register() {
        // Blocks
        RepairTableRegister.register();
        OvenRegister.register();
        SmelteryRegister.register();
        CauldronRegister.register();
        DeepCavePortalRegister.register();
        SieveRegister.register();
        DryingRackRegister.register();
        ResonantAltarRegistry.register();
        ResonantPedestalRegistry.register();
        BlockVarious.register();

        // Items
        CopperTools.register();
        FlintTools.register();
        SeedsBagItem.register();
        GlassBottles.register();
        Crops.register();
        SmelteryDie.register();
        Hilts.register();
        Plates.register();
        RepairKits.register();
        Rods.register();
        GemItem.register();
        EscapeRope.register();
        ItemVarious.register();

        SilverSet.register();
    }
}
