package com.lucab.shadows_over_elaria.block.repair_table;

import com.lucab.shadows_over_elaria.Utils;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RepairTable extends BaseEntityBlock {
    public static final MapCodec<RepairTable> CODEC = simpleCodec(RepairTable::new);

    public RepairTable(Properties properties) {
        super(properties);
    }

    public RepairTable() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {

        RepairTableEntity table = (RepairTableEntity) level.getBlockEntity(pos);
        if (table == null)
            return ItemInteractionResult.FAIL;

        if (!player.isShiftKeyDown()) {
            if (Utils.isRepairKit(stack)) {
                ItemStack remainder = table.insertKit(stack);
                if (remainder.getCount() != stack.getCount())
                    player.setItemInHand(hand, remainder);
            } else if (Utils.isItemToRepair(stack)) {
                ItemStack remainder = table.insertItem(stack);
                if (remainder.getCount() != stack.getCount())
                    player.setItemInHand(hand, remainder);
            } else if (Utils.isRepairHammer(stack)) {
                if (table.repair(level, pos, state)) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
        } else {
            if (table.hasItem()) {
                ItemStack removedItem = table.removeItem();
                if (player.getInventory().add(removedItem))
                    player.drop(removedItem, false);
            } else if (table.hasKit()) {
                ItemStack removedKit = table.removeKit();
                if (player.getInventory().add(removedKit))
                    player.drop(removedKit, false);
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        RepairTableEntity repairTable = (RepairTableEntity) level.getBlockEntity(pos);
        for (int i = 0; i < repairTable.inventory.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                    repairTable.inventory.getStackInSlot(i));
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.9375, 0.125, 0.875, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.125, 0.3125, 0.6875, 0.9375, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.125, 0.9375), BooleanOp.OR);
        return shape;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RepairTableEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

}
