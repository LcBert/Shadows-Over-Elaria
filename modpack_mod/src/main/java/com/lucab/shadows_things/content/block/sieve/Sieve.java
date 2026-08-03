package com.lucab.shadows_things.content.block.sieve;

import com.lucab.shadows_things.content.block.BlockVarious;
import com.lucab.shadows_things.content.item.SeedsBagHelper;
import com.lucab.shadows_things.rpg.professions.ProfessionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class Sieve extends Block {
    public Sieve() {
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(1.0F)
        );
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (stack.is(BlockVarious.UNDERGROUND_ROOTS_ITEM)) {
                int farmerLevel = ProfessionHelper.getLevel(player, ProfessionHelper.Professions.FARMER);
                Item selectedItem = SeedsBagHelper.allowedSeedsPerSlot.get(level.random.nextInt(SeedsBagHelper.allowedSeedsPerSlot.size()));
                int dropCount = 1 + level.random.nextInt(Math.max(1, farmerLevel));
                ItemEntity drop = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                        new ItemStack(selectedItem, dropCount)
                );
                if (level.addFreshEntity(drop)) {
                    level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS);

                    BlockState rootsState = BlockVarious.UNDERGROUND_ROOTS.get().defaultBlockState();
                    int blockStateId = Block.getId(rootsState);
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.above(), blockStateId);

                    if (!player.isCreative()) stack.shrink(1);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
