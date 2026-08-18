package com.lucab.shadows_things;

import com.lucab.shadows_things.attachments.ClassActionAttachments;
import com.lucab.shadows_things.content.ContentRegister;
import com.lucab.shadows_things.menus.MenuRegistries;
import com.lucab.shadows_things.rpg.classes.*;
import com.lucab.shadows_things.rpg.professions.OpenProfessionGuiPacket;
import com.lucab.shadows_things.rpg.professions.UpgradeProfessionPacket;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.lucab.shadows_things.rpg.gems.GemDataReader;
import com.lucab.shadows_things.rpg.gems.SocketRegistries;
import com.lucab.shadows_things.rpg.professions.ProfessionAttachments;
import com.lucab.shadows_things.rpg.professions.ProfessionCommand;
import com.lucab.shadows_things.toast.ToastCommand;
import com.lucab.shadows_things.toast.ToastPacket;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.lucab.shadows_things.attachments.ExhaustionAttachments;
import com.lucab.shadows_things.loot.AddTreeBarkModifier;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ShadowsThings.MODID)
public class ShadowsThings {
    public static final String MODID = "shadows_things";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ClassDataReader CLASS_READER = new ClassDataReader();
    public static final GemDataReader GEM_READER = new GemDataReader();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    // Data Components
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    // Menu
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    // In your Mod Loot Modifier class or Registry class
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister
            .create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "shadows_things");

    // This text "add_tree_bark" MUST match the "type" in your JSON
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTreeBarkModifier>> ADD_BARK_CODEC = LOOT_MODIFIER_SERIALIZERS
            .register("add_tree_bark", () -> AddTreeBarkModifier.CODEC);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public ShadowsThings(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::registerPayLoad);

        // Content Register
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ContentRegister.register();

        // Data Components Register
        DATA_COMPONENTS.register(modEventBus);
        SocketRegistries.register();

        // Menus register
        MENUS.register(modEventBus);
        MenuRegistries.register();

        //Recipes register
        RecipesRegistries.register(modEventBus);

        // Attachment register
        ExhaustionAttachments.register();
        ClassActionAttachments.register();
        ProfessionAttachments.register();
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

        registrar.playToClient(
                SyncPlayerClassPacket.TYPE,
                SyncPlayerClassPacket.STREAM_CODEC,
                SyncPlayerClassPacket::handle
        );

        registrar.playToServer(
                ClassActionExecutePacket.TYPE,
                ClassActionExecutePacket.STREAM_CODEC,
                ClassActionExecutePacket::handle
        );

        // Profession
        registrar.playToServer(
                OpenProfessionGuiPacket.TYPE,
                OpenProfessionGuiPacket.STREAM_CODEC,
                OpenProfessionGuiPacket::handle
        );

        registrar.playToServer(
                UpgradeProfessionPacket.TYPE,
                UpgradeProfessionPacket.STREAM_CODEC,
                UpgradeProfessionPacket::handle
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
        event.addListener(GEM_READER);
    }
}
