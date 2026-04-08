ServerEvents.recipes(event => {
    event.remove("magistuarmory:leather_strip")
    event.shapeless("magistuarmory:leather_strip", ["minecraft:leather", { "tag": "c:tools/knife" }])
})