package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MenuRegistries {
    // Oven Menu
    public static final DeferredHolder<MenuType<?>, MenuType<OvenMenu>> OVEN_MENU = ShadowsThings.MENUS.register("oven_menu",
            () -> IMenuTypeExtension.create(OvenMenu::new));

    // Profession Menu
    public static final DeferredHolder<MenuType<?>, MenuType<ProfessionMenu>> PROFESSION_MENU = ShadowsThings.MENUS.register("profession_menu",
            ()->IMenuTypeExtension.create(ProfessionMenu::new));

    public static void register() {
    }
}
