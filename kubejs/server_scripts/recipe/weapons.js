ServerEvents.recipes(event => {
    event.remove({mod: "magistuarmory"})

    const stone_key = {
        "#": {"item": "minecraft:stone"},
        "@": {"item": "magistuarmory:hilt"},
    }

    weaponsSet("magistuarmory:heavymace", [
        " ##",
        " @#",
        "@  "
    ])
    weaponsSet("magistuarmory:claymore", [
        " ##",
        "#@#",
        "@# "
    ])

    /**
     *
     * @param {string} base_name
     * @param {string[]} pattern
     */
    function weaponsSet(base_name, pattern) {
        const materials = {
            "stone": ["minecraft:stone", "magistuarmory:hilt"],
            "copper": ["shadows_things:copper_plate", "shadows_things:copper_hilt"],
            "iron": ["shadows_things:iron_plate", "shadows_things:iron_hilt"],
            "gold": ["shadows_things:gold_plate", "shadows_things:gold_hilt"],
            "silver": ["shadows_things:silver_plate", "shadows_things:silver_hilt"],
            "netherite": ["shadows_things:netherite_plate", "shadows_things:netherite_hilt",]
        }

        for (const [material, ingredients] of Object.entries(materials)) {
            let key_set = {
                "#": {item: ingredients[0]},
                "@": {item: ingredients[1]},
            }
            let completeWeapon = getNamespace(base_name) + ":" + material + "_" + getName(base_name)
            event.shaped(completeWeapon, pattern, key_set)
        }
    }

    function getNamespace(string) {
        return string.split(":")[0]
    }

    function getName(string) {
        return string.split(":")[1]
    }
})