ServerEvents.recipes(event => {
    event.remove({type: "farmersdelight:cooking"})
    event.remove("farmersdelight:cooking_pot")
})