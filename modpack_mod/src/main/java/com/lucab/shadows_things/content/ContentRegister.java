package com.lucab.shadows_things.content;

import com.lucab.shadows_things.content.block.BlockVarious;
import com.lucab.shadows_things.content.block.deep_cave_portal_block.DeepCavePortalRegister;
import com.lucab.shadows_things.content.block.oven.OvenRegister;
import com.lucab.shadows_things.content.block.repair_table.RepairTableRegister;
import com.lucab.shadows_things.content.item.*;

public class ContentRegister {
    public static void register() {
        // Blocks
        RepairTableRegister.register();
        OvenRegister.register();
        DeepCavePortalRegister.register();
        BlockVarious.register();

        // Items
        CopperTools.register();
        FlintTools.register();
        Crops.register();
        Hilts.register();
        Plates.register();
        RepairKits.register();
        Rods.register();
        ItemVarious.register();

        SilverSet.register();
    }
}
