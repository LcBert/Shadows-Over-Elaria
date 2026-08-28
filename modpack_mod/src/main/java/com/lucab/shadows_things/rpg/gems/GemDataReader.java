package com.lucab.shadows_things.rpg.gems;

import com.google.gson.*;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GemDataReader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<ResourceLocation, GemDefinition.Gem> GEMS = new HashMap<>();

    public GemDataReader() {
        super(GSON, "gems");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GemDefinition.Gem> newGems = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();

                JsonObject upgradeObj = json.getAsJsonObject("upgrades");
                List<GemDefinition.Upgrade> upgrades = parseUpgrades(upgradeObj);

                JsonObject attrObj = json.getAsJsonObject("attributes");
                List<GemAttribute> weaponAttrs = parseAttributes(attrObj, "weapon");
                List<GemAttribute> armorAttrs = parseAttributes(attrObj, "armor");
                List<GemAttribute> toolAttrs = parseAttributes(attrObj, "tool");

                newGems.put(id, new GemDefinition.Gem(upgrades, weaponAttrs, armorAttrs, toolAttrs));
            } catch (Exception e) {
                ShadowsThings.LOGGER.error("Failed to load gem JSON: {}", id, e);
            }
        }

        GEMS.clear();
        GEMS.putAll(newGems);
        ShadowsThings.LOGGER.info("Loaded {} gems definitions", GEMS.size());
    }

    private List<GemDefinition.Upgrade> parseUpgrades(JsonObject parent) {
        String upgradeKey = "from%dto%d";
        List<GemDefinition.Upgrade> upgrades = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            int processTime = 200;
            List<Item> reagents = new ArrayList<>();
            List<Item> offerings = new ArrayList<>();

            int fromTier = i;
            int toTier = fromTier + 1;

            String upgradeFormatedKey = String.format(upgradeKey, fromTier, toTier);
            if (parent.has(upgradeFormatedKey)) {
                JsonObject upgradeObj = parent.getAsJsonObject(upgradeFormatedKey);
                if (upgradeObj.has("process_time")) {
                    processTime = upgradeObj.get("process_time").getAsInt();
                }

                if (upgradeObj.has("reagents")) {
                    for (JsonElement reagentJsonElement : upgradeObj.getAsJsonArray("reagents")) {
                        ResourceLocation itemRL = ResourceLocation.tryParse(reagentJsonElement.getAsString());
                        if (itemRL != null) reagents.add(BuiltInRegistries.ITEM.get(itemRL));
                    }
                }

                if (upgradeObj.has("offerings")) {
                    for (JsonElement offeringJsonElement : upgradeObj.getAsJsonArray("offerings")) {
                        ResourceLocation itemRL = ResourceLocation.tryParse(offeringJsonElement.getAsString());
                        if (itemRL != null) offerings.add(BuiltInRegistries.ITEM.get(itemRL));
                    }
                }
            }
            upgrades.add(new GemDefinition.Upgrade(fromTier, toTier, processTime, reagents, offerings));
        }
        return upgrades;
    }

    private List<GemAttribute> parseAttributes(JsonObject parent, String key) {
        List<GemAttribute> list = new ArrayList<>();
        if (parent.has(key)) {
            JsonArray array = parent.getAsJsonArray(key);
            for (JsonElement elem : array) {
                JsonObject obj = elem.getAsJsonObject();
                ResourceLocation attrId = ResourceLocation.parse(obj.get("attribute").getAsString());
                AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(obj.get("operation").getAsString().toUpperCase());
                double baseVal = obj.get("base_value").getAsDouble();
                list.add(new GemAttribute(attrId, op, baseVal));
            }
        }
        return list;
    }

    public Optional<GemDefinition.Gem> getGemDefinition(ItemStack stack) {
        return getGemDefinition(GemData.getGemId(stack));
    }

    public Optional<GemDefinition.Gem> getGemDefinition(ResourceLocation id) {
        return Optional.ofNullable(GEMS.get(id));
    }

    public Map<ResourceLocation, GemDefinition.Gem> getGems() {
        return Collections.unmodifiableMap(GEMS);
    }
}
