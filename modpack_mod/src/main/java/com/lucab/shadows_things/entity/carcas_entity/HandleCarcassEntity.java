package com.lucab.shadows_things.entity.carcas_entity;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.recipe.CarcassCuttingRecipe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = ShadowsThings.MODID)
public class HandleCarcassEntity {

    @SubscribeEvent
    public static void handleCarcassSpawn(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();

        if (level.isClientSide()) return;
        if (!CarcassCuttingRecipe.hasRecipe(level, entity.getType())) return;

        // Check if the entity is a LivingEntity and was removed via death
        if (entity instanceof LivingEntity living && living.getRemovalReason() == Entity.RemovalReason.KILLED) {
            CarcassEntity carcass = CarcassEntityRegistry.CARCASS_ENTITY.get().create(level);
            if (carcass != null) {
                carcass.setCopiedEntityType(living.getType());
                carcass.setSpawnTick(level.getGameTime());

                float bodyYaw = living.yBodyRot;
                carcass.moveTo(living.getX(), living.getY(), living.getZ(), bodyYaw, 0.0F);
                carcass.setYRot(bodyYaw);
                carcass.yRotO = bodyYaw;

                level.addFreshEntity(carcass);
            }
        }
    }

    @SubscribeEvent
    public static void handleMobDrop(LivingDropsEvent event) {
        LivingEntity living = event.getEntity();
        Level level = living.level();

        if (level.isClientSide()) return;
        if (living instanceof Player) return;
        if (CarcassCuttingRecipe.hasRecipe(level, living.getType())) event.setCanceled(true);
    }
}