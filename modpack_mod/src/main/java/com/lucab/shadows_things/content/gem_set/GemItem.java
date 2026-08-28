package com.lucab.shadows_things.content.gem_set;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.rpg.gems.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GemItem extends Item {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
    public static final DeferredItem<Item> GEM_ITEM = ShadowsThings.ITEMS.register("gem", GemItem::new);

    public GemItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack getRandomGem(int rarity) {
        ItemStack stack = new ItemStack(GEM_ITEM.get());
        List<ResourceLocation> gemIds = new ArrayList<>(ShadowsThings.GEM_READER.getGems().keySet().stream().toList());
        Collections.shuffle(gemIds);
        stack.set(SocketRegistries.GEM_DATA.get(), new GemData(gemIds.getFirst(), rarity));
        return stack;
    }

    public static ItemStack createGem(ResourceLocation gemId, int rarity) {
        ItemStack stack = new ItemStack(GEM_ITEM.get());
        stack.set(SocketRegistries.GEM_DATA.get(), new GemData(gemId, rarity));
        return stack;
    }

    public static List<ItemStack> getGems() {
        List<ItemStack> stacks = new ArrayList<>();

        for (ResourceLocation gemId : GemDataReader.GEMS.keySet()) {
            for (int rarity = 1; rarity <= 5; rarity++) {
                stacks.add(createGem(gemId, rarity));
            }
        }
        return stacks;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(SocketRegistries.GEM_DATA.get())) {
            GemData data = stack.get(SocketRegistries.GEM_DATA.get());
            return Component.translatable(String.format("item.shadows_things.gem.%s", data.gemId().toString().split(":")[1].toLowerCase()));
        }
        return Component.translatable("item.shadows_things.gem.generic");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!stack.has(SocketRegistries.GEM_DATA.get())) {
            tooltipComponents.add(Component.translatable("tooltip.shadows_things.unattuned_gem").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        GemData data = stack.get(SocketRegistries.GEM_DATA.get());
        var gemDefOpt = ShadowsThings.GEM_READER.getGemDefinition(data.gemId());

        if (gemDefOpt.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.shadows_things.unknown").withStyle(ChatFormatting.RED));
            return;
        }

        GemDefinition.Gem gemDef = gemDefOpt.get();
        int tier = data.rarity();

        // 1. Grado / Tier
        tooltipComponents.add(Component.translatable("tooltip.shadows_things.quality").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("tooltip.shadows_things.tier", tier).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("tooltip.shadows_things.socket_effects").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));

        // 2. Rendering Sezione Armi
        if (!gemDef.weaponAttributes().isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.shadows_things.when_socketed_weapon").withStyle(ChatFormatting.RED));
            for (GemAttribute attr : gemDef.weaponAttributes()) {
                tooltipComponents.add(formatAttributeLine(attr, tier));
            }
        }

        // 3. Rendering Sezione Armature
        if (!gemDef.armorAttributes().isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.shadows_things.when_socketed_armor").withStyle(ChatFormatting.BLUE));
            for (GemAttribute attr : gemDef.armorAttributes()) {
                tooltipComponents.add(formatAttributeLine(attr, tier));
            }
        }

        // 4. Rendering Sezione Utensili
        if (!gemDef.toolAttributes().isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.shadows_things.when_socketed_tool").withStyle(ChatFormatting.YELLOW));
            for (GemAttribute attr : gemDef.toolAttributes()) {
                tooltipComponents.add(formatAttributeLine(attr, tier));
            }
        }
    }

    /**
     * Formatta e colora una singola riga di attributo leggendo la traduzione nativa dell'Attribute
     */
    private Component formatAttributeLine(GemAttribute gemAttr, int tier) {
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(gemAttr.attributeId());

        // Nome tradotto dell'attributo Vanilla/Moddato (es. "Attack Damage", "Max Health")
        Component attrName = attribute != null ? Component.translatable(attribute.getDescriptionId()) : Component.literal(gemAttr.attributeId().getPath());

        double value = gemAttr.getValueForTier(tier);
        boolean isPercentage = gemAttr.operation() != AttributeModifier.Operation.ADD_VALUE;

        String formattedValue;
        if (isPercentage) {
            formattedValue = (value >= 0 ? "+" : "") + FORMAT.format(value * 100.0D) + "%";
        } else {
            formattedValue = (value >= 0 ? "+" : "") + FORMAT.format(value);
        }

        ChatFormatting valueColor = value >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

        return Component.literal("  • ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(formattedValue + " ").withStyle(valueColor))
                .append(attrName.copy().withStyle(ChatFormatting.GRAY));
    }

    public static void register() {
    }
}
