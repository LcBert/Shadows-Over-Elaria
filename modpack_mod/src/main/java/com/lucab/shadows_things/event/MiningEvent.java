package com.lucab.shadows_things.event;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.content.item.FlintTools;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent;

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
    public static void handleSaveTool(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.level();

        if (level.isClientSide || player.isCreative()) return;

        ItemStack mainHandItem = player.getMainHandItem();

        if (!mainHandItem.is(ItemTags.PICKAXES) || !mainHandItem.isDamageableItem()) return;

        BlockState state = event.getState();

        if (!state.is(Tags.Blocks.ORES)) return;

        int minerLevel = ProfessionHelper.getLevel(player, ProfessionHelper.Professions.MINER);
        if (minerLevel < 0) return;

        float saveToolChance = ProfessionHelper.MINER_CHANCE.saveTool.getPol(minerLevel);
        if (level.random.nextDouble() < saveToolChance) {
            int currentDamage = mainHandItem.getDamageValue();
            if (currentDamage > 0) mainHandItem.setDamageValue(currentDamage - 1);
        }

        float gainedXp = ProfessionHelper.MINER_CHANCE.oreXp.getPol(minerLevel);
        ProfessionHelper.addExperience(player, ProfessionHelper.Professions.MINER, (int) gainedXp);
    }

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void logsBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().isCreative())
            return;

        Level level = event.getPlayer().level();
        BlockState state = event.getState();
        boolean isLog = state.is(BlockTags.LOGS);
        boolean isFlintAxe = event.getPlayer().getMainHandItem().is(FlintTools.FLINT_AXE);

        if (isLog && isFlintAxe) {
            event.setCanceled(true);
            level.removeBlock(event.getPos(), false);

            Item treeBarkItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("farmersdelight", "tree_bark"));
            Block.popResource(level, event.getPos(), treeBarkItem.getDefaultInstance());

            event.getPlayer().getMainHandItem().hurtAndBreak(1, event.getPlayer(), EquipmentSlot.MAINHAND);
        }
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
