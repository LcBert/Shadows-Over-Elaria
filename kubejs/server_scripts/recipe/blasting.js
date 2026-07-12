ServerEvents.recipes(event => {
    event.remove({ type: "minecraft:blasting" })
    event.remove("minecraft:blast_furnace")
})