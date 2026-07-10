package com.lucab.shadows_things.rpg_class;

import com.google.gson.*;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.*;

public class RpgClassDataReader extends SimpleJsonResourceReloadListener {

    public record RpgClassData(String className, Map<Integer, List<Item>> tiers) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String TARGET_NAMESPACE = "shadows_things";

    public Map<String, RpgClassData> rpgClasses = new HashMap<>();

    public RpgClassDataReader() {
        super(GSON, "rpg_class");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, RpgClassData> newClasses = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resLoc = entry.getKey();

            if (!resLoc.getNamespace().equals(TARGET_NAMESPACE)) {
                continue;
            }

            String className = resLoc.getPath().toLowerCase();

            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                Map<Integer, List<Item>> tiersMap = new HashMap<>();

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
                newClasses.put(className, new RpgClassData(className, tiersMap));
            } catch (Exception e) {
                ShadowsThings.LOGGER.error("Error while parsing RPG Class datapack for file: {}", resLoc, e);
            }
        }

        this.rpgClasses = newClasses;
        ShadowsThings.LOGGER.info("Successfully loaded {} RPG Classes from datapacks", this.rpgClasses.size());
    }

    public Optional<RpgClassData> getClassData(String className) {
        return Optional.ofNullable(this.rpgClasses.get(className.toLowerCase()));
    }

    public Map<String, RpgClassData> getAllClasses() {
        return Collections.unmodifiableMap(this.rpgClasses);
    }
}