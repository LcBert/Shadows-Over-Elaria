package com.lucab.shadows_things.content.block.resonant.resonant_pedestal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ResonantPedestalBlock extends BaseEntityBlock {
    public static final MapCodec<ResonantPedestalBlock> CODEC = simpleCodec(ResonantPedestalBlock::new);

    public ResonantPedestalBlock(Properties properties) {
        super(properties);
    }

    public ResonantPedestalBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.AMETHYST)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        );
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof ResonantPedestalBlockEntity pedestal)) return;

        // Custom amethyst / deep purple tinted dust (RGB normalized: 0.72F, 0.25F, 0.95F)
        DustParticleOptions resonantDust = new DustParticleOptions(new Vector3f(0.72F, 0.25F, 0.95F), 0.85F);

        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 1.25D;
        double centerZ = pos.getZ() + 0.5D;

        long gameTime = level.getGameTime();
        float partialTicks = random.nextFloat(); // Adds micro-jitter for smoother distribution

        if (pedestal.hasAltar()) {
            BlockPos altarPos = pedestal.getAltar();

            // Coordinates: Start above pedestal, end above altar
            double startX = pos.getX() + 0.5D;
            double startY = pos.getY() + 1.25D;
            double startZ = pos.getZ() + 0.5D;

            double endX = altarPos.getX() + 0.5D;
            double endY = altarPos.getY() + 1.5D; // Adjust according to altar height
            double endZ = altarPos.getZ() + 0.5D;

            // Vector from pedestal to altar
            double dx = endX - startX;
            double dy = endY - startY;
            double dz = endZ - startZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.001D) {
                // Normal vector along the line
                double normX = dx / distance;
                double normY = dy / distance;
                double normZ = dz / distance;

                // Orthogonal vectors for transverse circular motion
                Vec3 forward = new Vec3(normX, normY, normZ);
                Vec3 up = Math.abs(normY) < 0.95D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
                Vec3 right = forward.cross(up).normalize();
                Vec3 orthogonalUp = right.cross(forward).normalize();

                // 1. Dynamic Moving Beam (Particles travelling from pedestal to altar)
                int streamParticles = 6;
                double flowSpeed = 0.08D;

                for (int i = 0; i < streamParticles; i++) {
                    // Dynamic progression parameter t in range [0, 1]
                    double progress = ((gameTime + partialTicks) * flowSpeed + (i / (double) streamParticles)) % 1.0D;

                    // Parabolic vertical arc for energy tether gravity/sag
                    double arcOffset = Math.sin(progress * Math.PI) * 0.35D;

                    // Spiral wave along the trajectory
                    double spiralRadius = 0.12D * Math.sin(progress * Math.PI); // Pinched at start/end
                    double spiralAngle = (gameTime + partialTicks) * 0.4D + (progress * Math.PI * 4.0D);

                    double currentX = startX + dx * progress + (right.x * Math.cos(spiralAngle) + orthogonalUp.x * Math.sin(spiralAngle)) * spiralRadius;
                    double currentY = startY + dy * progress + arcOffset + (right.y * Math.cos(spiralAngle) + orthogonalUp.y * Math.sin(spiralAngle)) * spiralRadius;
                    double currentZ = startZ + dz * progress + (right.z * Math.cos(spiralAngle) + orthogonalUp.z * Math.sin(spiralAngle)) * spiralRadius;

                    // Tangential velocity carrying the particle forward
                    double particleVelX = normX * 0.05D;
                    double particleVelY = normY * 0.05D;
                    double particleVelZ = normZ * 0.05D;

                    level.addParticle(resonantDust, currentX, currentY, currentZ, particleVelX, particleVelY, particleVelZ);
                }

                // 2. High-Frequency Micro Energy Sparks (Sparks skipping across the beam)
                if (random.nextFloat() < 0.4F) {
                    double sparkProgress = random.nextDouble();
                    double sparkX = startX + dx * sparkProgress + (random.nextDouble() - 0.5D) * 0.1D;
                    double sparkY = startY + dy * sparkProgress + Math.sin(sparkProgress * Math.PI) * 0.35D + (random.nextDouble() - 0.5D) * 0.1D;
                    double sparkZ = startZ + dz * sparkProgress + (random.nextDouble() - 0.5D) * 0.1D;

                    level.addParticle(
                            ParticleTypes.SCULK_CHARGE_POP,
                            sparkX, sparkY, sparkZ,
                            (random.nextDouble() - 0.5D) * 0.02D,
                            0.01D,
                            (random.nextDouble() - 0.5D) * 0.02D
                    );
                }

                // 3. Ambient Tether Mist (Witch magic mist drifting along the arc)
                if (random.nextFloat() < 0.25F) {
                    double mistProgress = random.nextDouble();
                    double mistX = startX + dx * mistProgress;
                    double mistY = startY + dy * mistProgress + Math.sin(mistProgress * Math.PI) * 0.35D;
                    double mistZ = startZ + dz * mistProgress;

                    level.addParticle(
                            ParticleTypes.WITCH,
                            mistX, mistY, mistZ,
                            normX * 0.02D,
                            0.01D,
                            normZ * 0.02D
                    );
                }
            }
        }

        if (pedestal.hasItem()) {
            // 1. Dual-Helix Orbiting Resonant Dust around the floating item
            double orbitRadius = 0.45D;
            double orbitSpeed = 0.12D;

            for (int arm = 0; arm < 2; arm++) {
                double currentAngle = (gameTime + partialTicks) * orbitSpeed + (arm * Math.PI);
                double offsetX = Math.cos(currentAngle) * orbitRadius;
                double offsetZ = Math.sin(currentAngle) * orbitRadius;
                // Subtle vertical oscillation on the helix
                double offsetY = Math.sin((gameTime + partialTicks) * 0.08D + (arm * Math.PI)) * 0.1D;

                level.addParticle(
                        resonantDust,
                        centerX + offsetX,
                        centerY + offsetY,
                        centerZ + offsetZ,
                        0.0D, 0.005D, 0.0D
                );
            }

            // 2. Inward Converging Enchantment Glyphs
            if (random.nextFloat() < 0.6F) {
                double spawnAngle = random.nextDouble() * Math.PI * 2.0D;
                double spawnDist = 0.7D + random.nextDouble() * 0.3D;
                double spawnHeight = centerY + (random.nextDouble() - 0.5D) * 0.5D;

                double glyphX = centerX + Math.cos(spawnAngle) * spawnDist;
                double glyphZ = centerZ + Math.sin(spawnAngle) * spawnDist;

                // Velocity vector pointing directly to item core
                double velX = (centerX - glyphX) * 0.08D;
                double velY = (centerY - spawnHeight) * 0.08D;
                double velZ = (centerZ - glyphZ) * 0.08D;

                level.addParticle(ParticleTypes.ENCHANT, glyphX, spawnHeight, glyphZ, velX, velY, velZ);
            }

            // 3. Resonant Pulse / Spark bursts
            if (random.nextFloat() < 0.15F) {
                // Sculk charge pop creates a crisp resonant 'snap' effect
                level.addParticle(
                        ParticleTypes.SCULK_CHARGE_POP,
                        centerX + (random.nextDouble() - 0.5D) * 0.2D,
                        centerY + (random.nextDouble() - 0.5D) * 0.2D,
                        centerZ + (random.nextDouble() - 0.5D) * 0.2D,
                        (random.nextDouble() - 0.5D) * 0.01D,
                        0.02D,
                        (random.nextDouble() - 0.5D) * 0.01D
                );
            }

            // 4. Subtle Base Uplift (Ascending particles from the pedestal top to the item)
            if (random.nextFloat() < 0.25F) {
                double baseRadius = 0.25D;
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double baseParticleX = centerX + Math.cos(angle) * (random.nextDouble() * baseRadius);
                double baseParticleZ = centerZ + Math.sin(angle) * (random.nextDouble() * baseRadius);
                double baseParticleY = pos.getY() + 0.95D;

                level.addParticle(
                        ParticleTypes.WITCH,
                        baseParticleX,
                        baseParticleY,
                        baseParticleZ,
                        0.0D,
                        0.035D,
                        0.0D
                );
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos
            pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ResonantPedestalBlockEntity pedestal = (ResonantPedestalBlockEntity) level.getBlockEntity(pos);
        if (pedestal == null) return ItemInteractionResult.FAIL;

        // Insert Item
        if (!player.isShiftKeyDown()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (!itemInHand.isEmpty()) {
                if (pedestal.insertItem(itemInHand))
                    itemInHand.shrink(player.getAbilities().instabuild ? 0 : 1);
            }
        } else {
            ItemStack retrievedItem = pedestal.removeItem();
            if (!player.getInventory().add(retrievedItem))
                player.drop(retrievedItem, false);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                           boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof ResonantPedestalBlockEntity pedestal) {
            pedestal.notifyAltars();
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                            boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ResonantPedestalBlockEntity pedestal) {
                pedestal.notifyAltars();
                for (int i = 0; i < pedestal.inventory.getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestal.inventory.getStackInSlot(i));
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantPedestalBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
