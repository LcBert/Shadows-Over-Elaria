package com.lucab.shadows_things.content.block.cauldron;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class CauldronRegister {
    public static final DeferredBlock<CauldronBlock> CAULDRON = ShadowsThings.BLOCKS.register(
            "cauldron", () -> new CauldronBlock());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CauldronBlockEntity>> CAULDRON_BLOCK_ENTITY = ShadowsThings.BLOCK_ENTITIES.register(
            "cauldron", () -> BlockEntityType.Builder.of(CauldronBlockEntity::new, CAULDRON.get()).build(null));

    public static final DeferredItem<BlockItem> CAULDRON_ITEM = ShadowsThings.ITEMS.register("cauldron",
            () -> new BlockItem(CAULDRON.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(CAULDRON_ITEM.get())
        );
    }

}
