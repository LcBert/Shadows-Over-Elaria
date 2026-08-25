package com.lucab.shadows_things.event.modification;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.Map;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class ModifyToolMaxDamage {
    private static final Map<String, Integer> TOOLS_MAX_DAMAGE = Map.of(
            "magistuarmory:stone_katzbalger", 120,
            "minecraft:stone_pickaxe", 120,
            "minecraft:stone_axe", 120,
            "minecraft:stone_shovel", 120,
            "minecraft:stone_hoe", 120,
            "shadows_things:copper_pickaxe", 256,
            "minecraft:iron_pickaxe", 512,
            "minecraft:golden_pickaxe", 768,
            "minecraft:diamond_pickaxe", 1024,
            "minecraft:netherite_pickaxe", 1536
    );

    @SubscribeEvent
    public static void modifyDataComponents(ModifyDefaultComponentsEvent event) {
        for (Map.Entry<String, Integer> entry : TOOLS_MAX_DAMAGE.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
            event.modify(item, builder -> builder.set(DataComponents.MAX_DAMAGE, entry.getValue()));
        }
    }
}
