ServerEvents.recipes(event => {
    toolSet("minecraft:stone", ["minecraft:stone", "magistuarmory:hilt"])
    toolSet("minecraft:iron", ["shadows_things:iron_plate", "shadows_things:iron_hilt"])
    toolSet("shadows_things:copper", ["shadows_things:copper_plate", "shadows_things:copper_hilt"])
    toolSet("minecraft:golden", ["shadows_things:gold_plate", "shadows_things:gold_hilt"])
    toolSet("minecraft:diamond", ["shadows_things:silver_plate", "shadows_things:silver_hilt"])
    toolSet("minecraft:netherite", ["shadows_things:netherite_plate", "shadows_things:netherite_hilt"])

    /**
     *
     * @param {string} base_name
     * @param {string[]} ingredients
     */
    function toolSet(base_name, ingredients) {
        const toolsPattern = [
            {"pickaxe": ["###", " @ ", " @ "]},
            {"axe": ["## ", "#@ ", " @ "]},
            {"shovel": ["#", "@", "@"]},
            {"hoe": ["##", " @", " @"]}
        ]

        const key_set = {
            "#": {item: ingredients[0]},
            "@": {item: ingredients[1]}
        };

        for (const toolObj of toolsPattern) {
            for (const [tool, pattern] of Object.entries(toolObj)) {
                let completeTool = base_name + "_" + tool

                event.remove({output: completeTool})
                event.shaped(completeTool, pattern, key_set)
            }
        }
    }
})