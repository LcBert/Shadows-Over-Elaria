package com.lucab.shadows_things.rpg.gems;

import com.google.gson.*;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class GemDataReader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<ResourceLocation, GemDefinition> GEMS = new HashMap<>();

    public GemDataReader() {
        super(GSON, "gems");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GemDefinition> newGems = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                int color = Integer.decode(json.get("color").getAsString());

                JsonObject attrObj = json.getAsJsonObject("attributes");
                List<GemAttribute> weaponAttrs = parseAttributes(attrObj, "weapon");
                List<GemAttribute> armorAttrs = parseAttributes(attrObj, "armor");
                List<GemAttribute> toolAttrs = parseAttributes(attrObj, "tool");

                newGems.put(id, new GemDefinition(color, weaponAttrs, armorAttrs, toolAttrs));
            } catch (Exception e) {
                ShadowsThings.LOGGER.error("Failed to load gem JSON: {}", id, e);
            }
        }

        GEMS.clear();
        GEMS.putAll(newGems);
        ShadowsThings.LOGGER.info("Loaded {} gems definitions", GEMS.size());
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

    public static Optional<GemDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(GEMS.get(id));
    }
}
