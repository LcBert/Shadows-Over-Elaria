package com.lucab.shadows_things;

import org.slf4j.Logger;

import com.lucab.shadows_things.attachments.ExhaustionAttachments;
import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.item.Crops;
import com.lucab.shadows_things.item.FlintTools;
import com.lucab.shadows_things.item.Hilts;
import com.lucab.shadows_things.item.Plates;
import com.lucab.shadows_things.item.RepairKits;
import com.lucab.shadows_things.item.Rods;
import com.lucab.shadows_things.loot.AddTreeBarkModifier;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ShadowsThings.MODID)
public class ShadowsThings {
    public static final String MODID = "shadows_things";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    // In your Mod Loot Modifier class or Registry class
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister
            .create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "shadows_things");

    // This name "add_tree_bark" MUST match the "type" in your JSON
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTreeBarkModifier>> ADD_BARK_CODEC = LOOT_MODIFIER_SERIALIZERS
            .register("add_tree_bark", () -> AddTreeBarkModifier.CODEC);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public ShadowsThings(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Items register
        FlintTools.register();
        Crops.register();
        Plates.register();
        Rods.register();
        Hilts.register();
        RepairKits.register();

        // Blocks register
        BlocksRegister.register();

        // Attachment register
        ExhaustionAttachments.register();
    }
}
