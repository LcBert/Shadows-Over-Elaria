package com.lucab.shadows_things.rpg.classes;

import com.google.gson.*;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

public class ClassDataReader extends SimpleJsonResourceReloadListener {
    public record StarterKit(
            Map<EquipmentSlot, Item> armorItems,
            List<ItemStack> inventoryItems
    ) {
    }

    public record ClassAttribute(
            ResourceLocation attributeId,
            AttributeModifier.Operation operation,
            double value
    ) {
    }

    public record ClassData(
            String className,
            StarterKit starterKit,
            Map<Integer, List<Item>> tiers,
            List<ClassAttribute> attributes
    ) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String TARGET_NAMESPACE = "shadows_things";

    public Map<String, ClassData> rpgClasses = new HashMap<>();

    public ClassDataReader() {
        super(GSON, "rpg_class");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, ClassData> newClasses = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resLoc = entry.getKey();

            if (!resLoc.getNamespace().equals(TARGET_NAMESPACE)) {
                continue;
            }

            String className = resLoc.getPath().toLowerCase();

            try {
                Map<EquipmentSlot, String> armorsTypes = new HashMap<>(Map.of(
                        EquipmentSlot.HEAD, "head",
                        EquipmentSlot.CHEST, "chest",
                        EquipmentSlot.LEGS, "legs",
                        EquipmentSlot.FEET, "feet"
                ));

                Map<EquipmentSlot, Item> armorsItems = new HashMap<>();
                List<ItemStack> inventoryItems = new ArrayList<>();
                StarterKit starterKit = new StarterKit(armorsItems, inventoryItems);
                Map<Integer, List<Item>> tiersMap = new HashMap<>();
                List<ClassAttribute> attributesList = new ArrayList<>();

                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                if (jsonObject.has("tiers")) {
                    JsonObject tiersObj = jsonObject.getAsJsonObject("tiers");

                    for (int i = 1; i <= 5; i++) {
                        String tierKey = String.valueOf(i);
                        List<Item> items = new ArrayList<>();

                        if (tiersObj.has(tierKey)) {
                            JsonArray tierArray = tiersObj.getAsJsonArray(tierKey);
                            for (JsonElement element : tierArray) {
                                String itemString = element.getAsString();
                                ResourceLocation itemKey = ResourceLocation.parse(itemString);

                                // Directly converts to Item from the game registry
                                Item item = BuiltInRegistries.ITEM.get(itemKey);
                                if (item != BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) {
                                    items.add(item);
                                }
                            }
                        }
                        tiersMap.put(i, items);
                    }
                }

                if (jsonObject.has("attributes")) {
                    JsonObject attributesObj = jsonObject.getAsJsonObject("attributes");
                    for (Map.Entry<String, JsonElement> attrEntry : attributesObj.entrySet()) {
                        ResourceLocation attributeId = ResourceLocation.parse(attrEntry.getKey());
                        JsonObject modifierObj = attrEntry.getValue().getAsJsonObject();

                        String opStr = modifierObj.get("operation").getAsString().toUpperCase();
                        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(opStr);

                        double value = modifierObj.get("value").getAsDouble();

                        attributesList.add(new ClassAttribute(attributeId, operation, value));
                    }
                }

                if (jsonObject.has("starter_kit")) {
                    JsonObject starterKitObj = jsonObject.getAsJsonObject("starter_kit");
                    if (starterKitObj.has("armor")) {
                        JsonObject armorObj = starterKitObj.getAsJsonObject("armor");
                        armorsTypes.forEach((slot, name) -> {
                            if (armorObj.has(name)) {
                                String itemString = armorObj.get(name).getAsString();
                                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemString));
                                armorsItems.put(slot, item);
                            }
                        });
                    }
                    if (starterKitObj.has("inventory")) {
                        JsonArray inventoryArray = starterKitObj.getAsJsonArray("inventory");
                        for (JsonElement element : inventoryArray) {
                            JsonObject itemObj = element.getAsJsonObject();

                            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemObj.get("item").getAsString()));
                            int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                            inventoryItems.add(new ItemStack(item, count));
                        }
                    }
                }

                newClasses.put(className, new ClassData(className, starterKit, tiersMap, attributesList));
            } catch (Exception e) {
                ShadowsThings.LOGGER.error("Error while parsing RPG Class datapack for file: {}", resLoc, e);
            }
        }

        this.rpgClasses = newClasses;
        ShadowsThings.LOGGER.info("Successfully loaded {} RPG Classes from datapacks", this.rpgClasses.size());
    }

    public Optional<ClassData> getClassData(String className) {
        return Optional.ofNullable(this.rpgClasses.get(className.toLowerCase()));
    }

    public Map<String, ClassData> getAllClasses() {
        return Collections.unmodifiableMap(this.rpgClasses);
    }
}