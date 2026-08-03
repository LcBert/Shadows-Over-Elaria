package com.lucab.shadows_things.content.block.sieve;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class SieveRegister {
    public static final DeferredBlock<Sieve> SIEVE = ShadowsThings.BLOCKS.register("sieve", Sieve::new);

    public static final DeferredItem<BlockItem> SIEVE_ITEM = ShadowsThings.ITEMS.register("sieve",
            () -> new BlockItem(SIEVE.get(), new Item.Properties()));

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SIEVE_ITEM.get())
        );
    }
}
