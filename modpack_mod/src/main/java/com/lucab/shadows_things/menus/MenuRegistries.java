package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MenuRegistries {
    // Oven Menu
    public static final DeferredHolder<MenuType<?>, MenuType<OvenMenu>> OVEN_MENU = ShadowsThings.MENUS.register("oven_menu",
            () -> IMenuTypeExtension.create(OvenMenu::new));

    // Smeltery Menu
    public static final DeferredHolder<MenuType<?>, MenuType<SmelteryMenu>> SMELTERY_MENU = ShadowsThings.MENUS.register("smeltery_menu",
            () -> IMenuTypeExtension.create(SmelteryMenu::new));

    // Cauldron Menu
    public static final DeferredHolder<MenuType<?>, MenuType<CauldronMenu>> CAULDRON_MENU = ShadowsThings.MENUS.register("cauldron_menu",
            () -> IMenuTypeExtension.create(CauldronMenu::new));

    // Profession Menu
    public static final DeferredHolder<MenuType<?>, MenuType<ProfessionMenu>> PROFESSION_MENU = ShadowsThings.MENUS.register("profession_menu",
            () -> IMenuTypeExtension.create(ProfessionMenu::new));

    // Drying Rack Menu
    public static final DeferredHolder<MenuType<?>, MenuType<DryingRackMenu>> DRYING_RACK_MENU = ShadowsThings.MENUS.register("drying_rack_menu",
            () -> IMenuTypeExtension.create(DryingRackMenu::new));

    // Seeds bag Menu
    public static final DeferredHolder<MenuType<?>, MenuType<SeedsBagMenu>> SEEDS_BAG_MENU = ShadowsThings.MENUS.register("seeds_bag_menu",
            () -> IMenuTypeExtension.create(SeedsBagMenu::new));

    public static void register() {
    }
}
