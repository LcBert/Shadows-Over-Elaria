package com.lucab.shadows_things.dungeon.spawns;

import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;

import java.util.List;
import java.util.Optional;

public class DungeonSpawnConfig {
    private final SimpleWeightedRandomList<DungeonSpawnEntry> spawnPool;
    private final int minSpawns;
    private final int maxSpawns;

    public DungeonSpawnConfig(int minSpawns, int maxSpawns, List<DungeonSpawnEntry> entries) {
        this.minSpawns = minSpawns;
        this.maxSpawns = maxSpawns;

        SimpleWeightedRandomList.Builder<DungeonSpawnEntry> builder = SimpleWeightedRandomList.builder();
        for (DungeonSpawnEntry entry : entries) {
            builder.add(entry, entry.weight());
        }
        this.spawnPool = builder.build();
    }

    public Optional<DungeonSpawnEntry> getRandomEntry(RandomSource random) {
        return this.spawnPool.getRandomValue(random);
    }

    public int getMinSpawns() {
        return minSpawns;
    }

    public int getMaxSpawns() {
        return maxSpawns;
    }
}
