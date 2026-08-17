package com.lucab.shadows_things.client.screen;

import com.lucab.shadows_things.menus.CauldronMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CauldronScreen extends BaseMachineScreen<CauldronMenu> {
    public CauldronScreen(CauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, "cauldron",
                new int[]{119, 27}, new int[]{80, 53}, new int[]{22, 16}, ProgressDirection.RIGHT);
    }
}
