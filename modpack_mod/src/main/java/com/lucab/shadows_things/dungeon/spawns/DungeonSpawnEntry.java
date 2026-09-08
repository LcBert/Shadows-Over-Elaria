package com.lucab.shadows_things.dungeon.spawns;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;

public record DungeonSpawnEntry(
        String entity,
        int weight
) implements WeightedEntry {
    public static final Codec<DungeonSpawnEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("entity").forGetter(DungeonSpawnEntry::entity),
                    Codec.INT.optionalFieldOf("weight", 1).forGetter(DungeonSpawnEntry::weight)
            ).apply(instance, DungeonSpawnEntry::new));

    public EntityType<?> getEntityType() {
        ResourceLocation entityLoc = ResourceLocation.tryParse(entity);
        if (entityLoc == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
    }

    public int getWeightValue() {
        return this.weight;
    }

    @Override
    public Weight getWeight() {
        return Weight.of(this.weight);
    }
}
