ServerEvents.recipes(event => {
    event.remove({ type: "farmersdelight:cutting" })

    cuttingRecipe(event, "minecraft:beef", ["farmersdelight:minced_beef"])
    cuttingRecipe(event, "minecraft:porkchop", ["farmersdelight:bacon"])
    cuttingRecipe(event, "minecraft:mutton", ["farmersdelight:mutton_chops"])
    cuttingRecipe(event, "minecraft:chicken", ["farmersdelight:chicken_cuts"])
    cuttingRecipe(event, "minecraft:cod", ["farmersdelight:cod_slice"])
    cuttingRecipe(event, "minecraft:salmon", ["farmersdelight:salmon_slice"])
    cuttingRecipe(event, "farmersdelight:cabbage", ["farmersdelight:cabbage_leaf"])
})


function cuttingRecipe(event, input, outputs) {
    event.custom({
        "type": "farmersdelight:cutting",
        "ingredients": [{ "item": input }],
        "result": outputs.map(output => ({ "item": Item.of(output).toJson() })),
        "tool": { "tag": "c:tools/knife" }
    })
}
