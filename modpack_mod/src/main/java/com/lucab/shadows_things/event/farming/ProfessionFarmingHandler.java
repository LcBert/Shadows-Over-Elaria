package com.lucab.shadows_things.event.farming;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ProfessionFarmingHandler {
    @SubscribeEvent
    public static void onCropHarvest(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getPlayer().isCreative()) return;

        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        ItemStack productDrop = ItemStack.EMPTY;

        for (CropLootTables.CropLootType type : CropLootTables.CropLootType.values()) {
            Block cropBlock = BuiltInRegistries.BLOCK.get(type.blockId);
            if (cropBlock.equals(block)) {
                productDrop = new ItemStack(BuiltInRegistries.ITEM.get(type.result));
                break;
            }
        }

        if (productDrop.isEmpty()) return;

        if (block instanceof CropBlock cropBlock && cropBlock.isMaxAge(state) && player.getMainHandItem().is(ItemTags.HOES)) {
            int farmerLevel = ProfessionHelper.getLevel(player, ProfessionHelper.Professions.FARMER);

            // Handle extra drop chance
            double extraDropChance = ProfessionHelper.getPol(ProfessionHelper.FARMER_CHANCE.extra_drop, farmerLevel);
            if (level.random.nextDouble() < extraDropChance) {
                int extraAmount = level.random.nextInt(farmerLevel) + 1;
                ItemEntity bonusDrop = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                        new ItemStack(productDrop.getItem(), extraAmount)
                );
                level.addFreshEntity(bonusDrop);
            }

            // Handle Experience
            float gainedXp = ProfessionHelper.getPol(ProfessionHelper.EXPERIENCE_PER_LEVEL, farmerLevel);
            ProfessionHelper.addExperience(player, (int) gainedXp);
            ProfessionHelper.tryLevelUp(player, true, true);
        }
    }
}
