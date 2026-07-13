ServerEvents.recipes(event => {
    ovenRecipe(event, {"item": "minecraft:beef", "count": 1}, {"id": "minecraft:cooked_beef", "count": 1})
})

function ovenRecipe(event, input, output, time) {
    if (time == null) time = 200
    event.custom({
        "type": "shadows_things:oven_cooking",
        "ingredient": input,
        "result": output,
        "cooking_time": time
    })
}