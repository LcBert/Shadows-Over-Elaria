package com.lucab.shadows_things.event.farming;

import com.lucab.shadows_things.ShadowsThings;
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

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class CropLootTables {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected enum CropLootType {
        /**
         * lootTable resourceLocation
         * seed item id
         * result item id
         * block id
         */
        WHEAT("minecraft:blocks/wheat", "minecraft:wheat_seeds", "minecraft:wheat", "minecraft:wheat"),
        CARROT("minecraft:blocks/carrots", "shadows_things:carrot_seeds", "minecraft:carrot", "minecraft:carrots"),
        POTATO("minecraft:blocks/potatoes", "shadows_things:potato_seeds", "minecraft:potato", "minecraft:potatoes"),
        BEETROOT("minecraft:blocks/beetroots", "minecraft:beetroot_seeds", "minecraft:beetroot", "minecraft:beetroots"),
        ONION("farmersdelight:blocks/onions", "shadows_things:onion_seeds", "farmersdelight:onion", "farmersdelight:onions"),
        CABBAGE("farmersdelight:blocks/cabbages", "farmersdelight:cabbage_seeds", "farmersdelight:cabbage", "farmersdelight:cabbages");

        protected final ResourceLocation lootTable;
        protected final ResourceLocation seed;
        protected final ResourceLocation result;
        protected final ResourceLocation blockId;

        CropLootType(String lootTable, String seed, String result, String blockId) {
            this.lootTable = ResourceLocation.parse(lootTable);
            this.seed = ResourceLocation.parse(seed);
            this.result = ResourceLocation.parse(result);
            this.blockId = ResourceLocation.parse(blockId);
        }
    }

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        for (CropLootType type : CropLootType.values()) {
            if (event.getName().equals(type.lootTable)) {

                Item outputItem = BuiltInRegistries.ITEM.get(type.result);
                Item seedItem = BuiltInRegistries.ITEM.get(type.seed);
                Block block = BuiltInRegistries.BLOCK.get(type.blockId);

                if (block instanceof CropBlock cropBlock) {
                    int maxAge = cropBlock.getMaxAge();

                    // Creiamo una nuova tabella da zero, ignorando completamente quella vecchia
                    LootTable.Builder newTable = LootTable.lootTable();

                    LootPool.Builder pool = LootPool.lootPool()
                            .name("shadows_crop_control")
                            .setRolls(ConstantValue.exactly(1))
                            // Condizione 1: Prodotto (Solo se Maturo + Zappa)
                            .add(LootItem.lootTableItem(outputItem)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(CropBlock.AGE, maxAge)))
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.HOES)))
                            )
                            // Condizione 2: Seme (Se NON maturo OR senza Zappa)
                            .add(LootItem.lootTableItem(seedItem)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(CropBlock.AGE, maxAge)).invert()) // NON maturo
                            )
                            .add(LootItem.lootTableItem(seedItem)
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.HOES)).invert()) // SENZA zappa
                            );

                    event.setTable(newTable.withPool(pool).build());
                }
            }
        }
    }
}