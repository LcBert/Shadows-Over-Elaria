package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class MiningEvent {
    private static final TagKey<Block> ORE_1 = BlockTags.create(ResourceLocation.parse("shadows_things:ore_tier/1"));
    private static final TagKey<Block> ORE_2 = BlockTags.create(ResourceLocation.parse("shadows_things:ore_tier/2"));
    private static final TagKey<Block> ORE_3 = BlockTags.create(ResourceLocation.parse("shadows_things:ore_tier/3"));
    private static final TagKey<Block> ORE_4 = BlockTags.create(ResourceLocation.parse("shadows_things:ore_tier/4"));
    private static final TagKey<Block> ORE_5 = BlockTags.create(ResourceLocation.parse("shadows_things:ore_tier/5"));

    private static final TagKey<Item> TOOL_1 = ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/1"));
    private static final TagKey<Item> TOOL_2 = ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/2"));
    private static final TagKey<Item> TOOL_3 = ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/3"));
    private static final TagKey<Item> TOOL_4 = ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/4"));
    private static final TagKey<Item> TOOL_5 = ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/5"));

    @SubscribeEvent
    public static void logsMineableWithAxes(LeftClickBlock event) {
        if (event.getEntity().isCreative())
            return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        boolean isLog = state.is(BlockTags.LOGS);
        boolean isAxe = event.getItemStack().is(ItemTags.create(ResourceLocation.parse("minecraft:axes")));

        if (isLog && !isAxe)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void mineableWithPickaxe(LeftClickBlock event) {
        if (event.getEntity().isCreative())
            return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        boolean isMineablePickaxe = state.is(BlockTags.create(ResourceLocation.parse("minecraft:mineable/pickaxe.json")));
        boolean isPickaxe = event.getItemStack().is(ItemTags.create(ResourceLocation.parse("minecraft:pickaxes")));

        if (isMineablePickaxe && !isPickaxe)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void mineralMining(LeftClickBlock event) {
        if (event.getEntity().isCreative())
            return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ItemStack stack = event.getEntity().getMainHandItem();

        int requiredTier = getRequiredTier(state);

        if (requiredTier == 0) return;

        int toolTier = getToolTier(stack);

        if (toolTier < requiredTier) event.setCanceled(true);
    }

    private static int getRequiredTier(BlockState state) {
        if (state.is(ORE_5)) return 5;
        if (state.is(ORE_4)) return 4;
        if (state.is(ORE_3)) return 3;
        if (state.is(ORE_2)) return 2;
        if (state.is(ORE_1)) return 1;
        return 0;
    }

    private static int getToolTier(ItemStack stack) {
        if (stack.is(TOOL_5)) return 5;
        if (stack.is(TOOL_4)) return 4;
        if (stack.is(TOOL_3)) return 3;
        if (stack.is(TOOL_2)) return 2;
        if (stack.is(TOOL_1)) return 1;
        return 0;
    }
}
