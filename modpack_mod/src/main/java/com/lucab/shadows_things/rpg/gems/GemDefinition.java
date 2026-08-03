package com.lucab.shadows_things.rpg.gems;

import java.util.List;

public record GemDefinition(
        String name,
        int color,
        List<GemAttribute> weaponAttributes,
        List<GemAttribute> armorAttributes,
        List<GemAttribute> toolAttributes
) {
}
