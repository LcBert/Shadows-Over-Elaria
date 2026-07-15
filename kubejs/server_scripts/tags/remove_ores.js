ServerEvents.tags("block", event => {
    event.get("minecraft:needs_stone_tool").remove([
        "minecraft:iron_block",
        "minecraft:raw_iron_block",
        "minecraft:iron_ore",
        "minecraft:deepslate_iron_ore",
        "minecraft:lapis_block",
        "minecraft:lapis_ore",
        "minecraft:deepslate_lapis_ore",
        "minecraft:copper_block",
        "minecraft:raw_copper_block",
        "minecraft:copper_ore",
        "minecraft:deepslate_copper_ore"
    ])

    event.get("minecraft:needs_iron_tool").remove([
        "minecraft:diamond_ore",
        "minecraft:deepslate_diamond_ore",
        "minecraft:emerald_ore",
        "minecraft:deepslate_emerald_ore",
        "minecraft:emerald_block",
        "minecraft:gold_block",
        "minecraft:raw_gold_block",
        "minecraft:gold_ore",
        "minecraft:deepslate_gold_ore",
        "minecraft:redstone_ore",
        "minecraft:deepslate_redstone_ore"
    ])

    event.get("minecraft:needs_diamond_tool").remove([
        "minecraft:ancient_debris"
    ])
})