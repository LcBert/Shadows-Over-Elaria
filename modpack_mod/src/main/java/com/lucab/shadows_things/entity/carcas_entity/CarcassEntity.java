package com.lucab.shadows_things.entity.carcas_entity;

import com.lucab.shadows_things.content.item.CarcassItem;
import com.lucab.shadows_things.recipe.CarcassCuttingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CarcassEntity extends Entity {
    private static final EntityDataAccessor<String> COPY_ENTITY_TYPE = SynchedEntityData.defineId(
            CarcassEntity.class, EntityDataSerializers.STRING
    );
    private static final EntityDataAccessor<Integer> CURRENT_INTERACTION_COUNT = SynchedEntityData.defineId(
            CarcassEntity.class, EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Integer> MAX_INTERACTION_COUNT = SynchedEntityData.defineId(
            CarcassEntity.class, EntityDataSerializers.INT
    );

    @Nullable
    private LivingEntity cachedClientEntity;

    public CarcassEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        Vec3 currentMovement = this.getDeltaMovement();
        double horizontalDrag = 0.98D;
        double verticalDrag = 0.98D;

        if (this.onGround()) {
            BlockPos groundPos = this.getBlockPosBelowThatAffectsMyMovement();
            BlockState groundState = this.level().getBlockState(groundPos);
            float blockFriction = groundState.getBlock().getFriction();
            horizontalDrag = blockFriction * 0.91F;
        }

        this.setDeltaMovement(currentMovement.multiply(horizontalDrag, verticalDrag, horizontalDrag));
    }

    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);

        // Pickup to item when sneaking with an empty hand
        if (player.isShiftKeyDown() && heldStack.isEmpty()) {
            if (!this.level().isClientSide) {
                EntityType<?> type = this.getCopiedEntityType();
                if (type != null) {
                    ItemStack carcassStack = CarcassItem.createForType(type, this.getCurrentInteractions());
                    if (!player.addItem(carcassStack)) {
                        player.drop(carcassStack, false);
                    }
                    this.discard();
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        EntityType<?> copiedType = this.getCopiedEntityType();
        if (copiedType == null) {
            return InteractionResult.PASS;
        }

        int nextStep = this.getCurrentInteractions() + 1;
        CarcassCuttingRecipe.CarcassCuttingRecipeInput recipeInput =
                new CarcassCuttingRecipe.CarcassCuttingRecipeInput(copiedType, heldStack, nextStep);

        // Query matching recipe step via helper
        return CarcassCuttingRecipe.getMatchingStep(this.level(), copiedType, heldStack, nextStep)
                .map(step -> {
                    if (!this.level().isClientSide) {
                        ItemStack dropStack = step.drop().copy();

                        if (!dropStack.isEmpty()) {
                            ItemEntity dropEntity = new ItemEntity(
                                    this.level(),
                                    this.getX(),
                                    this.getY() + 0.25D,
                                    this.getZ(),
                                    dropStack
                            );
                            dropEntity.setDefaultPickUpDelay();
                            this.level().addFreshEntity(dropEntity);
                        }

                        heldStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                        ServerLevel serverLevel = (ServerLevel) this.level();
                        serverLevel.sendParticles(
                                this.resolveBloodParticle(),
                                this.getX(), this.getY() + 0.2D, this.getZ(),
                                10, 0.25D, 0.15D, 0.25D, 0.05D
                        );

                        this.level().playSound(
                                null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.PUMPKIN_CARVE, SoundSource.PLAYERS,
                                1.0F, 1.0F
                        );

                        this.setCurrentInteractions(nextStep);

                        int maxInteractions = this.getMaxSteps();
                        if (maxInteractions <= 0) {
                            maxInteractions = CarcassCuttingRecipe.getMaxInteractions(this.level(), copiedType);
                        }

                        if (nextStep >= maxInteractions) {
                            this.discard();
                        }
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                })
                .orElse(InteractionResult.PASS);
    }

    private ParticleOptions resolveBloodParticle() {
        var rawParticle = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.fromNamespaceAndPath("epicfight", "blood"));
        return (rawParticle instanceof ParticleOptions options)
                ? options
                : ParticleTypes.DAMAGE_INDICATOR;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COPY_ENTITY_TYPE, "");
        builder.define(CURRENT_INTERACTION_COUNT, 0);
        builder.define(MAX_INTERACTION_COUNT, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (COPY_ENTITY_TYPE.equals(key)) {
            this.refreshDimensions();
        }
    }

    public void setCopiedEntityType(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        this.entityData.set(COPY_ENTITY_TYPE, key.toString());

        if (!this.level().isClientSide) {
            this.setMaxSteps(CarcassCuttingRecipe.getMaxInteractions(this.level(), type));
        }

        this.refreshDimensions();
    }

    @Nullable
    public EntityType<?> getCopiedEntityType() {
        String key = this.entityData.get(COPY_ENTITY_TYPE);
        if (key.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(key);
        return rl != null ? BuiltInRegistries.ENTITY_TYPE.get(rl) : null;
    }

    public void setCurrentInteractions(int count) {
        this.entityData.set(CURRENT_INTERACTION_COUNT, count);
    }

    public int getCurrentInteractions() {
        return this.entityData.get(CURRENT_INTERACTION_COUNT);
    }

    public void setMaxSteps(int maxSteps) {
        this.entityData.set(MAX_INTERACTION_COUNT, maxSteps);
    }

    public int getMaxSteps() {
        return this.entityData.get(MAX_INTERACTION_COUNT);
    }

    @Nullable
    public LivingEntity getOrCreateClientEntity() {
        if (!this.level().isClientSide) return null;

        EntityType<?> type = getCopiedEntityType();
        if (type == null) {
            this.cachedClientEntity = null;
            return null;
        }

        if (this.cachedClientEntity == null || this.cachedClientEntity.getType() != type) {
            Entity created = type.create(this.level());
            if (created instanceof LivingEntity living) {
                this.cachedClientEntity = living;
            }
        }
        return this.cachedClientEntity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityType<?> copiedType = this.getCopiedEntityType();
        if (copiedType != null) {
            EntityDimensions originalDims = copiedType.getDimensions();
            float horizontalSpan = Math.max(originalDims.width(), originalDims.height());
            float verticalSpan = originalDims.width();
            return EntityDimensions.scalable(horizontalSpan, verticalSpan);
        }
        return super.getDimensions(pose);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("CopiedType")) {
            this.entityData.set(COPY_ENTITY_TYPE, tag.getString("CopiedType"));
            this.refreshDimensions();
        }
        if (tag.contains("CurrentInteractions")) {
            this.setCurrentInteractions(tag.getInt("CurrentInteractions"));
        }
        if (tag.contains("MaxInteractions")) {
            this.setMaxSteps(tag.getInt("MaxInteractions"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("CopiedType", this.entityData.get(COPY_ENTITY_TYPE));
        tag.putInt("CurrentInteractions", this.getCurrentInteractions());
        tag.putInt("MaxInteractions", this.getMaxSteps());
    }
}