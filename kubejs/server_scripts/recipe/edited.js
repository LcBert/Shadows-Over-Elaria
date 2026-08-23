ServerEvents.recipes(event => {
    event.remove("minecraft:smithing_table")

    event.shaped("minecraft:smithing_table", [
        "CC",
        "PP",
        "PP"
    ], {
        C: {item: "minecraft:copper_ingot"},
        P: {tag: "minecraft:planks"},
    })
})