ServerEvents.recipes(event => {
    event.remove({ type: "minecraft:smelting" })

    event.smelting("minecraft:stone", "minecraft:cobblestone")
})