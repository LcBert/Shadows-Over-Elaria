package com.lucab.shadows_things.spawns;

import com.google.gson.*;
import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public class SpawnsDataReader extends SimpleJsonResourceReloadListener {
    public record SpawnData(
            EntityType<?> entityType,
            int[] count,
            int[] light,
            SpawnProperties spawnProperties,
            Set<ResourceKey<Level>> dimensions,
            List<BiomeFilter> biomes
    ) {
    }

    public record SpawnProperties(
            boolean spawnOnPeaceful,
            boolean spawnOnlyOnNight,
            int spawnCap,
            float spawnChance,
            int spawnRadius,
            int safeRadius,
            int[] spawnHeight,
            int[] spawnY
    ) {
    }

    public record BiomeFilter(
            Optional<TagKey<Biome>> tag,
            Optional<ResourceLocation> biomeId
    ) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private Map<String, SpawnData> spawns = new HashMap<>();

    public SpawnsDataReader() {
        super(GSON, "spawns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, SpawnData> newSpawns = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resLoc = entry.getKey();
            if (!resLoc.getNamespace().equals(ShadowsThings.MODID)) continue;

            String spawnName = resLoc.getPath().toLowerCase();

            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                if (!jsonObject.has("entity") || !jsonObject.has("spawn_properties")) continue;

                String entityString = jsonObject.get("entity").getAsString();
                ResourceLocation entityId = ResourceLocation.tryParse(entityString);
                if (entityId == null) {
                    ShadowsThings.LOGGER.warn("Invalid entity ID '{}' in {}", entityString, resLoc);
                    continue;
                }

                Optional<EntityType<?>> entityTypeOpt = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
                if (entityTypeOpt.isEmpty()) {
                    ShadowsThings.LOGGER.warn("Unknown EntityType '{}' in {}", entityId, resLoc);
                    continue;
                }

                int count[] = parseCount(jsonObject);
                int[] light = parseLight(jsonObject);
                SpawnProperties spawnProperties = parseSpawnProperties(jsonObject);

                Set<ResourceKey<Level>> dimensions = new HashSet<>();
                if (jsonObject.has("dimensions")) {
                    JsonArray dimensionsArray = jsonObject.getAsJsonArray("dimensions");
                    for (JsonElement element : dimensionsArray) {
                        ResourceLocation dimLoc = ResourceLocation.tryParse(element.getAsString());
                        if (dimLoc != null) {
                            dimensions.add(ResourceKey.create(Registries.DIMENSION, dimLoc));
                        }
                    }
                }

                List<BiomeFilter> biomes = new ArrayList<>();
                if (jsonObject.has("biomes")) {
                    JsonArray biomesArray = jsonObject.getAsJsonArray("biomes");
                    for (JsonElement element : biomesArray) {
                        String b = element.getAsString();
                        if (b.startsWith("#")) {
                            ResourceLocation tagLoc = ResourceLocation.tryParse(b.substring(1));
                            if (tagLoc != null) {
                                biomes.add(new BiomeFilter(Optional.of(TagKey.create(Registries.BIOME, tagLoc)), Optional.empty()));
                            }
                        } else {
                            ResourceLocation bLoc = ResourceLocation.tryParse(b);
                            if (bLoc != null) {
                                biomes.add(new BiomeFilter(Optional.empty(), Optional.of(bLoc)));
                            }
                        }
                    }
                }

                newSpawns.put(spawnName, new SpawnData(
                        entityTypeOpt.get(),
                        count,
                        light,
                        spawnProperties,
                        dimensions,
                        biomes
                ));
            } catch (Exception e) {
                ShadowsThings.LOGGER.error("Error while parsing Spawns datapack for file: {}", resLoc, e);
            }
        }
        this.spawns = Collections.unmodifiableMap(newSpawns);
        ShadowsThings.LOGGER.info("Successfully loaded {} Spawns from datapack", this.spawns.size());
    }

    private int[] parseCount(JsonObject jsonObject) {
        if (jsonObject.has("count")) {
            JsonObject countObj = jsonObject.get("count").getAsJsonObject();
            int minCount = countObj.has("min") ? countObj.get("min").getAsInt() : 1;
            int maxCount = countObj.has("max") ? countObj.get("max").getAsInt() : Math.max(minCount, 1);
            return new int[]{Math.min(minCount, maxCount), Math.max(minCount, maxCount)};
        }
        return new int[]{1, 1};
    }

    private int[] parseLight(JsonObject jsonObject) {
        if (jsonObject.has("light")) {
            JsonObject lightObj = jsonObject.get("light").getAsJsonObject();
            int minLight = lightObj.has("min") ? lightObj.get("min").getAsInt() : 0;
            int maxLight = lightObj.has("max") ? lightObj.get("max").getAsInt() : 15;
            return new int[]{Math.min(minLight, maxLight), Math.max(minLight, maxLight)};
        }
        return new int[]{0, 15};
    }

    private SpawnProperties parseSpawnProperties(JsonObject jsonObject) {
        JsonObject obj = jsonObject.get("spawn_properties").getAsJsonObject();

        boolean spawnOnPeaceful = obj.has("spawn_on_peaceful") && obj.get("spawn_on_peaceful").getAsBoolean();
        boolean spawnOnlyOnNight = obj.has("spawn_only_on_night") && obj.get("spawn_only_on_night").getAsBoolean();
        int spawnCap = obj.has("spawn_cap") ? obj.get("spawn_cap").getAsInt() : 32;
        float spawnChance = obj.has("spawn_chance") ? obj.get("spawn_chance").getAsFloat() : 0.5f;
        int spawnRadius = obj.has("spawn_radius") ? obj.get("spawn_radius").getAsInt() : 64;
        int safeRadius = obj.has("safe_radius") ? obj.get("safe_radius").getAsInt() : 8;

        int[] spawnHeight = new int[]{-20, 20};
        if (obj.has("spawn_height")) {
            JsonObject h = obj.get("spawn_height").getAsJsonObject();
            int minH = h.has("min") ? h.get("min").getAsInt() : -20;
            int maxH = h.has("max") ? h.get("max").getAsInt() : 20;
            spawnHeight = new int[]{Math.min(minH, maxH), Math.max(minH, maxH)};
        }

        int[] spawnY = new int[]{64, 150};
        if (obj.has("spawn_y")) {
            JsonObject y = obj.get("spawn_y").getAsJsonObject();
            int minY = y.has("min") ? y.get("min").getAsInt() : 64;
            int maxY = y.has("max") ? y.get("max").getAsInt() : 150;
            spawnY = new int[]{Math.min(minY, maxY), Math.max(minY, maxY)};
        }

        return new SpawnProperties(
                spawnOnPeaceful,
                spawnOnlyOnNight,
                spawnCap,
                spawnChance,
                spawnRadius,
                safeRadius,
                spawnHeight,
                spawnY
        );
    }

    public Map<String, SpawnData> getSpawns() {
        return spawns;
    }
}
