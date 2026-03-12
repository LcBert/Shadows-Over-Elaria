package com.lucab.shadows_things.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddTreeBarkModifier extends LootModifier {
    public static final MapCodec<AddTreeBarkModifier> CODEC = RecordCodecBuilder
            .mapCodec(inst -> LootModifier.codecStart(inst).apply(inst, AddTreeBarkModifier::new));

    public AddTreeBarkModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);

        // 1. Check if the block is a log
        if (state != null && state.is(BlockTags.LOGS)) {
            boolean isLog = state.is(BlockTags.LOGS);

            // 2. Check if it's NOT a stripped log or wood (Check by name or tag)
            if (isLog) {
                generatedLoot.clear();
                generatedLoot.add(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("farmersdelight", "tree_bark"))));
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
