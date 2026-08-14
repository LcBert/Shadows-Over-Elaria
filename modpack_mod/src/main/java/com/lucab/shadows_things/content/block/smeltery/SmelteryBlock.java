package com.lucab.shadows_things.content.block.smeltery;

import com.lucab.shadows_things.menus.SmelteryMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmelteryBlock extends BaseEntityBlock {
    public static final MapCodec<SmelteryBlock> CODEC = simpleCodec(SmelteryBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final String[][] STRUCTURE = {{
            "XXX",
            "XXX",
            "XXX"
    }, {
            " X ",
            "X X",
            " C "
    }, {
            " X ",
            "X X",
            " X "
    }};
    private static final int[] CONTROLLER_OFFSET = new int[3]; // x, y, z

    static {
        boolean found = false;
        for (int y = 0; y < STRUCTURE.length; y++) {
            String[] layer = STRUCTURE[y];
            for (int z = 0; z < layer.length; z++) {
                String row = layer[z];
                int x = row.indexOf('C');
                if (x != -1) {
                    CONTROLLER_OFFSET[0] = x;
                    CONTROLLER_OFFSET[1] = y;
                    CONTROLLER_OFFSET[2] = z;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("Smeltery structure must contain a controller 'C'");
        }
    }

    private int tier;
    private Block baseBlock;

    public SmelteryBlock(Properties properties) {
        super(properties);
        registerDefaultState();
    }

    public SmelteryBlock(int tier, Block baseBlock) {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.5f)
                .noOcclusion()
        );
        registerDefaultState();
        this.tier = tier;
        this.baseBlock = baseBlock;
    }

    private void registerDefaultState() {
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            Direction facing = state.getValue(FACING).getOpposite();
            BlockPos behindPos = pos.relative(facing);

            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, behindPos.getX() + 0.5, behindPos.getY() + 0.5, behindPos.getZ() + 0.5, 0, 0.05, 0);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (level.getBlockState(pos).getBlock() instanceof SmelteryBlock smelteryBlock) {
                if (be instanceof SmelteryBlockEntity smelteryEntity) {
                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new SmelteryMenu(containerId, playerInventory, smelteryEntity),
                            Component.translatable(state.getBlock().getDescriptionId())
                                    .append(Component.literal(" | ")
                                            .append(Component.translatable("gui.shadows_things.smeltery_gui.tier", smelteryBlock.getTier()))
                                    )
                    ), pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public boolean validateStructure(Level level, BlockPos controllerPos, BlockState state) {
        Direction facing = state.getValue(FACING);
        int height = STRUCTURE.length;

        for (int y = 0; y < height; y++) {
            String[] layer = STRUCTURE[y];
            int depth = layer.length;
            for (int z = 0; z < depth; z++) {
                String row = layer[z];
                int width = row.length();
                for (int x = 0; x < width; x++) {
                    char expectedChar = row.charAt(x);
                    if (expectedChar == ' ') continue;

                    BlockPos targetPos = translateRelativeToAbsolute(controllerPos, x, y, z, facing);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (!matchesPredicate(targetState, expectedChar)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private BlockPos translateRelativeToAbsolute(BlockPos controllerPos, int relX, int relY, int relZ, Direction facing) {
        int offsetX = -CONTROLLER_OFFSET[0];
        int offsetY = -CONTROLLER_OFFSET[1];
        int offsetZ = -CONTROLLER_OFFSET[2];

        int localX = relX + offsetX;
        int localY = relY + offsetY;
        int localZ = relZ + offsetZ;

        // Rotate local coordinates based on the direction the controller is facing
        int rotX = localX;
        int rotZ = localZ;

        switch (facing) {
            case NORTH -> {
                rotX = -localX;
                rotZ = -localZ;
            }
            case SOUTH -> {
                // rotX and rotZ maintain standard or inverted signs depending on the layout
            }
            case WEST -> {
                rotX = -localZ;
                rotZ = localX;
            }
            case EAST -> {
                rotX = localZ;
                rotZ = -localX;
            }
            default -> {
            }
        }

        return controllerPos.offset(rotX, localY, rotZ);
    }

    private boolean matchesPredicate(BlockState state, char c) {
        if (state.isAir()) return false;

        return switch (c) {
            case 'X' -> state.is(this.baseBlock);
            case 'C' -> state.getBlock() instanceof SmelteryBlock;
            case ' ' -> state.isAir();
            default -> false;
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmelteryBlockEntity smelteryBlockEntity) {
                IItemHandler inventory = smelteryBlockEntity.getInventoryHandler();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public int getTier() {
        return tier;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, SmelteryRegister.SMELTERY_BLOCK_ENTITY.get(), SmelteryBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmelteryBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
