ServerEvents.recipes(event => {
    event.remove({ type: "minecraft:smoking" })
    event.remove("minecraft:smoker")

    event.shaped("minecraft:smoker", [
        "ABA",
        "B B",
        "ABA"
    ], {
        A: { tag: "minecraft:logs" },
        B: { item: "minecraft:stone" },
    })

    event.smoking("minecraft:cooked_beef", "minecraft:beef")
    event.smoking("minecraft:cooked_porkchop", "minecraft:porkchop")
    event.smoking("minecraft:cooked_mutton", "minecraft:mutton")
    event.smoking("minecraft:cooked_chicken", "minecraft:chicken")
    event.smoking("minecraft:cooked_cod", "minecraft:cod")
    event.smoking("minecraft:cooked_salmon", "minecraft:salmon")
})