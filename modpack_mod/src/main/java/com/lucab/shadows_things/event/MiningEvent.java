package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.item.FlintTools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class MiningEvent {
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
        boolean isStone = state.is(BlockTags.create(ResourceLocation.parse("c:stones")));
        boolean isCobblestone = state.is(BlockTags.create(ResourceLocation.parse("c:cobblestones")));
        boolean isMineablePickaxe = state.is(BlockTags.create(ResourceLocation.parse("minecraft:mineable/pickaxe")));
        boolean isPickaxe = event.getItemStack().is(ItemTags.create(ResourceLocation.parse("minecraft:pickaxes")));
        boolean isFlintPickaxe = event.getItemStack().getItem() == FlintTools.FLINT_PICKAXE.get();

        if ((isStone || isCobblestone) && isFlintPickaxe)
            event.setCanceled(true);

        if (isMineablePickaxe && !isPickaxe)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void mineralMining(LeftClickBlock event) {
        if (event.getEntity().isCreative())
            return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ItemStack item = event.getItemStack();

        System.out.println(item);

        boolean isCopperOre = state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE);
        boolean isIronOre = state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE);
        boolean isGoldOre = state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE);
        boolean isDiamondOre = state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
        boolean isNetheriteOre = state.is(Blocks.ANCIENT_DEBRIS);

        boolean isTier1 = item.is(ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/1")));
        boolean isTier2 = item.is(ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/2")));
        boolean isTier3 = item.is(ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/3")));
        boolean isTier4 = item.is(ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/4")));
        boolean isTier5 = item.is(ItemTags.create(ResourceLocation.parse("shadows_things:pickaxe_tier/5")));

        if (isCopperOre && !isTier1)
            event.setCanceled(true);

        if (isIronOre && !isTier2)
            event.setCanceled(true);

        if (isGoldOre && !isTier3)
            event.setCanceled(true);

        if (isDiamondOre && !isTier4)
            event.setCanceled(true);

        if (isNetheriteOre && !isTier5)
            event.setCanceled(true);
    }
}
