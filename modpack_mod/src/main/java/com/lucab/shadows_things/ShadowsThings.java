package com.lucab.shadows_things;

import com.lucab.shadows_things.content.ContentRegister;
import com.lucab.shadows_things.deep_cave.DeepCavePlayerAttachment;
import com.lucab.shadows_things.entity.carcas_entity.CarcassEntityRegistry;
import com.lucab.shadows_things.exhaustion.ExhaustionData;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.rpg.classes.*;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.lucab.shadows_things.rpg.gems.GemDataReader;
import com.lucab.shadows_things.rpg.gems.SocketRegistries;
import com.lucab.shadows_things.rpg.professions.ProfessionAttachments;
import com.lucab.shadows_things.rpg.professions.ProfessionCommand;
import com.lucab.shadows_things.spawns.SpawnsDataReader;
import com.lucab.shadows_things.toast.ToastCommand;
import com.lucab.shadows_things.toast.ToastPacket;
import com.lucab.shadows_things.worldgen.deep_cave.DeepCaveDimension;
import com.lucab.shadows_things.worldgen.deep_cave.DeepCaveNoiseSettings;
import com.lucab.shadows_things.worldgen.features_type.FeaturesRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod(ShadowsThings.MODID)
public class ShadowsThings {
    public static final String MODID = "shadows_things";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ClassDataReader CLASS_READER = new ClassDataReader();
    public static final SpawnsDataReader SPAWNS_READER = new SpawnsDataReader();
    public static final GemDataReader GEM_READER = new GemDataReader();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE, MODID);

    // Data Components
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // Menu
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    // Attachments
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public ShadowsThings(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::onGatherData);
        modEventBus.addListener(this::registerPayLoad);

        // Content Register
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ContentRegister.register();

        // Entity Register
        ENTITIES.register(modEventBus);
        CarcassEntityRegistry.register();

        // Data Components Register
        DATA_COMPONENTS.register(modEventBus);
        SocketRegistries.register();

        // Menus Register
        MENUS.register(modEventBus);
        MenuRegistries.register();

        //Recipes Register
        RecipesRegistries.register(modEventBus);

        // Attachment Register
        ATTACHMENT_TYPES.register(modEventBus);
        ExhaustionData.register();
        DeepCavePlayerAttachment.register();
        ClassPlayerData.register();
        ClassEntityData.register();
        ProfessionAttachments.register();

        // Features Types
        FeaturesRegistry.register(modEventBus);
    }

    public void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
                // Deep Cave Dimension
                .add(Registries.DIMENSION_TYPE, DeepCaveDimension::bootstrapType)
                .add(Registries.LEVEL_STEM, DeepCaveDimension::bootstrapStem)
                // Deep Cave Noise Settings
                .add(Registries.NOISE_SETTINGS, DeepCaveNoiseSettings::bootstrap);

        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, registrySetBuilder, Set.of(ShadowsThings.MODID)));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ToastCommand.register(event.getDispatcher());
        ClassCommand.register(event.getDispatcher());
        ProfessionCommand.register(event.getDispatcher());
    }

    public void registerPayLoad(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("5");

        // Class
        registrar.playToServer(
                ClassSelectPacket.TYPE,
                ClassSelectPacket.STREAM_CODEC,
                ClassSelectPacket::handle
        );

        registrar.playToServer(
                ClassActionExecutePacket.TYPE,
                ClassActionExecutePacket.STREAM_CODEC,
                ClassActionExecutePacket::handle
        );

        // Toast
        registrar.playToClient(
                ToastPacket.TYPE,
                ToastPacket.STREAM_CODEC,
                ToastPacket::handle
        );
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(CLASS_READER);
        event.addListener(SPAWNS_READER);
        event.addListener(GEM_READER);
    }
}
