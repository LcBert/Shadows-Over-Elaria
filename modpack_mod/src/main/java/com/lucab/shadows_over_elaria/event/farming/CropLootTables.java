package com.lucab.shadows_over_elaria.event.farming;

import com.lucab.shadows_over_elaria.ShadowsOverElaria;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;

@EventBusSubscriber(modid = ShadowsOverElaria.MODID)
public class CropLootTables {
    private static final Logger LOGGER = LogUtils.getLogger();

    private enum CropLootType {
        /**
         * lootTable resourceLocation
         * seed item id
         * result item id
         * block id
         */
        WHEAT("minecraft:blocks/wheat", "minecraft:wheat_seeds", "minecraft:wheat", "minecraft:wheat"),
        CARROT("minecraft:blocks/carrots", "shadows_over_elaria:carrot_seeds", "minecraft:carrot", "minecraft:carrots");

        private final ResourceLocation lootTable;
        private final String seed;
        private final String result;
        private final ResourceLocation blockId;

        CropLootType(String lootTable, String seed, String result, String blockId) {
            this.lootTable = ResourceLocation.parse(lootTable);
            this.seed = seed;
            this.result = result;
            this.blockId = ResourceLocation.parse(blockId);
        }
    }

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        for (CropLootType type : CropLootType.values()) {
            if (event.getName().equals(type.lootTable)) {

                try {
                    Field poolsField = ObfuscationReflectionHelper.findField(LootTable.class, "pools");
                    poolsField.setAccessible(true);

                    List<?> pools = (List<?>) poolsField.get(event.getTable());
                    pools.clear();

                    LOGGER.info("Successfully cleared vanilla pools for: {}", event.getName());
                } catch (Exception e) {
                    LOGGER.error("Failed to clear loot pools via reflection!", e);
                }

                Item outputItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(type.result));
                Item inputItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(type.seed));
                Block block = BuiltInRegistries.BLOCK.get(type.blockId);

                if (block instanceof CropBlock cropBlock) {
                    int maxAge = cropBlock.getMaxAge();

                    LootPool.Builder pool = LootPool.lootPool()
                            .name("so_elaria_logic")
                            .setRolls(ConstantValue.exactly(1))
                            .add(AlternativesEntry.alternatives(
                                    // Option 1: Grown + Hoe = Drop Result
                                    LootItem.lootTableItem(outputItem)
                                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties()
                                                            .hasProperty(CropBlock.AGE, maxAge)))
                                            .when(MatchTool
                                                    .toolMatches(ItemPredicate.Builder.item().of(ItemTags.HOES))),

                                    // Option 2: Fallback (Not grown OR no hoe) = Drop Seed
                                    LootItem.lootTableItem(inputItem)));

                    event.getTable().addPool(pool.build());
                }
            }
        }
    }
}