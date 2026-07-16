ServerEvents.recipes(event => {
    ovenRecipe(event, "minecraft:beef", "minecraft:cooked_beef", 1)
})

function ovenRecipe(event, input, output, time) {
    if (time == null) time = 200

    const inMatch = input.match(/^(?:(\d+)x\s*)?(.+)$/);
    const outMatch = output.match(/^(?:(\d+)x\s*)?(.+)$/);

    if (!inMatch || !outMatch) return

    let inCount = inMatch[1] ? parseInt(inMatch[1], 10) : 1
    let inItem = inMatch[2]

    let outCount = outMatch[1] ? parseInt(outMatch[1], 10) : 1
    let outItem = outMatch[2]

    event.custom({
        "type": "shadows_things:oven_cooking",
        "ingredient": {
            "item": inItem,
            "count": inCount
        },
        "result": {
            "id": outItem,
            "count": outCount
        },
        "cooking_time": time
    })
}