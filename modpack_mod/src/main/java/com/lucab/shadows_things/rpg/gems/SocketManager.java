package com.lucab.shadows_things.rpg.gems;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.classes.ClassItemsManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocketManager {
    public static int getMaxSocketsForItem(ItemStack stack) {
        int tier = ClassItemsManager.getItemTier(stack.getItem());
        return Math.max(tier, 0);
    }

    public static SocketDataComponent getOrCreateSocketData(ItemStack stack) {
        if (stack.isEmpty()) return new SocketDataComponent(0, new ArrayList<>());

        SocketDataComponent existingData = stack.get(SocketRegistries.SOCKET_COMPONENT.get());
        int maxSockets = getMaxSocketsForItem(stack);

        if (existingData != null) {
            if (existingData.maxSockets() != maxSockets) {
                SocketDataComponent updated = new SocketDataComponent(maxSockets, existingData.gems());
                stack.set(SocketRegistries.SOCKET_COMPONENT.get(), updated);
                rebuildAttributes(stack, updated);
                return updated;
            }
            return existingData;
        }

        SocketDataComponent newData = new SocketDataComponent(maxSockets, new ArrayList<>());
        stack.set(SocketRegistries.SOCKET_COMPONENT.get(), newData);
        rebuildAttributes(stack, newData);
        return newData;
    }

    public static boolean insertGem(ItemStack stack, GemSocket gemSocket) {
        SocketDataComponent socketData = getOrCreateSocketData(stack);
        if (socketData.canInsertGem()) {
            SocketDataComponent updatedData = socketData.withGem(gemSocket);
            stack.set(SocketRegistries.SOCKET_COMPONENT.get(), updatedData);

            rebuildAttributes(stack, updatedData);
            return true;
        }
        return false;
    }

    public static void addSocketSlot(ItemStack stack) {
        SocketDataComponent socketData = getOrCreateSocketData(stack);
        SocketDataComponent updatedDAta = socketData.withMaxSockets(socketData.maxSockets() + 1);
        stack.set(SocketRegistries.SOCKET_COMPONENT.get(), updatedDAta);
    }

    public static void rebuildAttributes(ItemStack itemStack, SocketDataComponent socketData) {
        // Copia gli attributi originali dell'item in modo da non perdere il danno base dell'arma/utensile
        ItemAttributeModifiers originalModifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder();

        if (originalModifiers != null) {
            for (ItemAttributeModifiers.Entry entry : originalModifiers.modifiers()) {
                attrBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        int socketIndex = 0;
        for (GemSocket socket : socketData.gems()) {
            GemData data = socket.gemData();
            Optional<GemDefinition> defOpt = GemDataReader.get(data.gemId());

            if (defOpt.isPresent()) {
                GemDefinition def = defOpt.get();
                int tier = data.rarity();

                List<GemAttribute> attributesToApply = getAttributesForEquipmentType(itemStack, def);

                for (GemAttribute gemAttr : attributesToApply) {
                    Attribute attribute = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(gemAttr.attributeId());
                    if (attribute != null) {
                        double scaledValue = gemAttr.getValueForTier(tier);

                        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                                ShadowsThings.MODID, "gem_slot_" + socketIndex);

                        AttributeModifier modifier = new AttributeModifier(
                                modifierId,
                                scaledValue,
                                gemAttr.operation()
                        );

                        var attributeHolder = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);

                        attrBuilder.add(attributeHolder, modifier, EquipmentSlotGroup.ANY);
                    }
                }
            }
            socketIndex++;
        }

        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
    }

    private static List<GemAttribute> getAttributesForEquipmentType(ItemStack stack, GemDefinition def) {
        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
            return def.weaponAttributes();
        } else if (stack.getItem() instanceof ArmorItem) {
            return def.armorAttributes();
        }
        return def.toolAttributes();
    }
}