package com.lucab.shadows_things.content.block.repair_table;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class RepairTableRegister {
    public static final DeferredBlock<RepairTable> REPAIR_TABLE = ShadowsThings.BLOCKS.register("repair_table",
            () -> new RepairTable());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepairTableEntity>> REPAIR_TABLE_ENTITY = ShadowsThings.BLOCK_ENTITIES
            .register("repair_table",
                    () -> BlockEntityType.Builder.of(RepairTableEntity::new,
                            REPAIR_TABLE.get()).build(null));

    public static final DeferredItem<BlockItem> REPAIR_TABLE_ITEM = ShadowsThings.ITEMS.register("repair_table",
            () -> new BlockItem(REPAIR_TABLE.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(REPAIR_TABLE_ITEM.get())
        );
    }
}
