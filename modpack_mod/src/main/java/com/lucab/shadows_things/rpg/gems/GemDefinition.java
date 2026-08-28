package com.lucab.shadows_things.rpg.gems;

import net.minecraft.world.item.Item;

import java.util.List;

public class GemDefinition {
    public record Upgrade(
            int fromTier,
            int toTier,
            int processTime,
            List<Item> reagents,
            List<Item> offerings
    ) {
    }

    public record Gem(
            List<Upgrade> upgrades,
            List<GemAttribute> weaponAttributes,
            List<GemAttribute> armorAttributes,
            List<GemAttribute> toolAttributes
    ) {
    }
}
