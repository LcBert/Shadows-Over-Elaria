package com.lucab.shadows_things.content.item;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.deep_cave.DeepCaveHelper;
import com.lucab.shadows_things.worldgen.DeepCave.DeepCaveDimension;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EscapeRope extends Item {
    public static final DeferredItem<Item> ESCAPE_ROPE = ShadowsThings.ITEMS.register(
            "escape_rope", EscapeRope::new);

    public EscapeRope() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.dimension().equals(DeepCaveDimension.DEEP_CAVE_LEVEL_KEY)) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 200;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        double posX = livingEntity.getX();
        double posY = livingEntity.getY();
        double posZ = livingEntity.getZ();

        int particlesPerTick = 4;
        for (int i = 0; i < particlesPerTick; i++) {
            double angle = (remainingUseDuration * 0.2) + (i * (Math.PI / 2));
            double radius = 3 * (1.0 - ((remainingUseDuration % 40) / 40.0)); // inward pull effect
            if (radius < 0.1) radius = 3;

            double x = posX + Math.cos(angle) * radius;
            double z = posZ + Math.sin(angle) * radius;
            double y = posY + ((remainingUseDuration % 40) * 0.1); // moves upward

            level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            DeepCaveHelper.exitPlayer(level, player);
            player.stopUsingItem();

            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.shadows_things.escape_rope.info").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }

    public static void register() {
    }

    public static ItemStack getItem() {
        return new ItemStack(ESCAPE_ROPE.get());
    }
}
