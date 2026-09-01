ServerEvents.recipes(event => {
    event.remove({ type: "minecraft:smelting" })

    event.smelting("minecraft:stone", "minecraft:cobblestone")
    event.smelting("minecraft:brick", "minecraft:clay_ball")
    event.smelting("minecraft:charcoal", "#minecraft:logs")
})