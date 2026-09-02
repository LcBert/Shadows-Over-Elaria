package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.client.renderer.carcass_renderer.CarcassItemRenderer;
import com.lucab.shadows_things.entity.carcas_entity.CarcassEntity;
import com.lucab.shadows_things.entity.carcas_entity.CarcassEntityRegistry;
import com.lucab.shadows_things.recipe.CarcassCuttingRecipe;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CarcassItem extends Item {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CARCASS_ENTITY_TYPE =
            ShadowsThings.DATA_COMPONENTS.register("carcass_entity_type", () ->
                    DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARCASS_INTERACTIONS =
            ShadowsThings.DATA_COMPONENTS.register("carcass_interactions", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CARCASS_SPAWN_TICK =
            ShadowsThings.DATA_COMPONENTS.register("carcass_spawn_tick", () ->
                    DataComponentType.<Long>builder()
                            .persistent(Codec.LONG)
                            .networkSynchronized(ByteBufCodecs.VAR_LONG)
                            .build());

    public static final DeferredItem<CarcassItem> CARCASS_ITEM = ShadowsThings.ITEMS.register("carcass_item", CarcassItem::new);

    public CarcassItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack createForType(EntityType<?> entityType, @Nullable SynchedEntityData entityData) {
        ItemStack stack = new ItemStack(CARCASS_ITEM.get());
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        stack.set(CARCASS_ENTITY_TYPE.get(), id.toString());
        if (entityData != null) {
            stack.set(CARCASS_INTERACTIONS.get(), entityData.get(CarcassEntity.CURRENT_INTERACTION_COUNT));
            stack.set(CARCASS_SPAWN_TICK.get(), entityData.get(CarcassEntity.SPAWN_TICK));
        }
        return stack;
    }

    public static ItemStack createForId(String entityId) {
        ResourceLocation entityKey = ResourceLocation.tryParse(entityId);
        if (entityKey == null) return ItemStack.EMPTY;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityKey);
        return createForType(type, null);
    }

    @Nullable
    public static EntityType<?> getCopiedType(ItemStack stack) {
        String typeId = stack.get(CARCASS_ENTITY_TYPE.get());
        if (typeId == null || typeId.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(typeId);
        return rl != null ? BuiltInRegistries.ENTITY_TYPE.get(rl) : null;
    }

    public static int getInteractions(ItemStack stack) {
        Integer interactions = stack.get(CARCASS_INTERACTIONS.get());
        return interactions != null ? interactions : 0;
    }

    public static long getSpawnTick(ItemStack stack) {
        Long spawnTick = stack.get(CARCASS_SPAWN_TICK.get());
        return spawnTick != null ? spawnTick : 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        EntityType<?> targetType = getCopiedType(stack);
        if (targetType == null) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        CarcassEntity carcass = CarcassEntityRegistry.CARCASS_ENTITY.get().create(level);
        if (carcass != null) {
            carcass.setCopiedEntityType(targetType);
            carcass.setCurrentInteractions(getInteractions(stack));

            long spawnTick = getSpawnTick(stack);
            if (spawnTick > 0) carcass.setSpawnTick(getSpawnTick(stack));
            else carcass.setSpawnTick(level.getGameTime());

            float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() : 0.0F;
            carcass.moveTo(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    yaw,
                    0.0F
            );
            carcass.setYRot(yaw);
            carcass.yRotO = yaw;

            level.addFreshEntity(carcass);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = getCopiedType(stack);
        if (type != null) {
            return Component.translatable("item.shadows_things.carcass_item.named", type.getDescription());
        }
        return super.getName(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CarcassItemRenderer.getInstance();
            }
        });
    }

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        List<ItemStack> carcassItems = new ArrayList<>();
        ClientLevel clientLevel = Minecraft.getInstance().level;

        if (clientLevel != null) {
            clientLevel.getRecipeManager()
                    .getAllRecipesFor(RecipesRegistries.CARCASS_CUTTING_TYPE.get())
                    .forEach(recipeHolder -> {
                        CarcassCuttingRecipe recipe = recipeHolder.value();
                        EntityType<?> type = recipe.getResolvedEntityType();
                        if (type != null) {
                            carcassItems.add(createForType(type, null));
                        }
                    });
        }
        return carcassItems;
    }
}
