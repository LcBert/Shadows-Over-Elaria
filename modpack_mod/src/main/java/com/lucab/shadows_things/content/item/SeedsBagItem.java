package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.menus.SeedsBagMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SeedsBagItem extends Item {
    public static final DeferredItem<Item> SEEDS_BAG = ShadowsThings.ITEMS.register("seeds_bag", SeedsBagItem::new);

    public SeedsBagItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack bagStack = player.getMainHandItem();
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new SeedsBagMenu(containerId, playerInventory, bagStack),
                    Component.translatable("item.shadows_things.seeds_bag")
            ));
            player.playNotifySound(SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(bagStack, level.isClientSide);
    }

    public static void register() {
    }

    public static List<ItemStack> getItems() {
        return List.of(
                new ItemStack(SEEDS_BAG.get())
        );
    }
}
