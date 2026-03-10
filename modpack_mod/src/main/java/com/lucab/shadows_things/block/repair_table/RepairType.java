package com.lucab.shadows_things.block.repair_table;

import com.lucab.shadows_things.item.RepairKits;

import net.minecraft.world.item.Item;

public enum RepairType {
    IRON_SWORD("minecraft:iron_sword", KitType.IRON);

    public final String item;
    public final KitType kit;

    RepairType(String item, KitType kit) {
        this.item = item;
        this.kit = kit;
    }

    public enum KitType {
        COPPER(RepairKits.COPPER_REPAIR_KIT.get()),
        IRON(RepairKits.IRON_REPAIR_KIT.get()),
        GOLD(RepairKits.GOLD_REPAIR_KIT.get()),
        DIAMOND(RepairKits.DIAMOND_REPAIR_KIT.get()),
        NETHERITE(RepairKits.NETHERITE_REPAIR_KIT.get());

        public final Item kit;

        KitType(Item kit) {
            this.kit = kit;
        }
    }
}
