package com.lucab.shadows_things;

import org.slf4j.Logger;

import com.lucab.shadows_things.attachments.ExhaustionAttachments;
import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.item.Crops;
import com.lucab.shadows_things.item.Hilts;
import com.lucab.shadows_things.item.Plates;
import com.lucab.shadows_things.item.RepairKits;
import com.lucab.shadows_things.item.Rods;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public ShadowsThings(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Items register
        Crops.register();
        Plates.register();
        Rods.register();
        Hilts.register();
        RepairKits.register();

        // Blocks register
        BlocksRegister.register();

        // Attackment register
        ExhaustionAttachments.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
