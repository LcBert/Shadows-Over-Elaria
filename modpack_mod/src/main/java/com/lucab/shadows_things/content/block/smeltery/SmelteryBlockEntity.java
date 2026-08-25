package com.lucab.shadows_things.content.block.smeltery;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.recipe.SmelteryRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SmelteryBlockEntity extends BlockEntity {
    public SmelteryBlockEntity(BlockPos pos, BlockState state) {
        super(SmelteryRegister.SMELTERY_BLOCK_ENTITY.get(), pos, state);
    }

    private int litTime;
    private int litDuration = 200;
    private int processTime;
    private int totalProcessTime = 200;

    private SmelteryRecipe.RecipeInstance recipeInstance = null;

    public final ItemStackHandler inventory = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // Input Slots
            if (slot >= 0 && slot <= 8) return true;

            // Die Slot
            if (slot == 9) return true;

            // Output Slot
            if (slot == 10) return false;

            // Fuel Slot (Only burnable items)
            if (slot == 11) return stack.getBurnTime(null) > 0;
            return super.isItemValid(slot, stack);
        }
    };

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SmelteryBlockEntity.this.litTime;
                case 1 -> SmelteryBlockEntity.this.litDuration;
                case 2 -> SmelteryBlockEntity.this.processTime;
                case 3 -> SmelteryBlockEntity.this.totalProcessTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> SmelteryBlockEntity.this.litTime = value;
                case 1 -> SmelteryBlockEntity.this.litDuration = value;
                case 2 -> SmelteryBlockEntity.this.processTime = value;
                case 3 -> SmelteryBlockEntity.this.totalProcessTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public int getLitTime() {
        return this.containerData.get(0);
    }

    public int getLitDuration() {
        return this.containerData.get(1);
    }

    public int getProcessTime() {
        return this.containerData.get(2);
    }

    public int getTotalProcessTime() {
        return this.containerData.get(3);
    }

    public IItemHandler getInventoryHandler() {
        return this.inventory;
    }

    public ContainerData getContainerData() {
        return this.containerData;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SmelteryBlockEntity smeltery) {
        if (level.isClientSide) return;

        smeltery.handleLitState();

        if (smeltery.canProcess()) {
            if (smeltery.isLit()) smeltery.processTime++;
            else if (level.getGameTime() % 20 == 0 && smeltery.processTime > 0) smeltery.processTime--;

            if (smeltery.processTime >= smeltery.totalProcessTime) {
                smeltery.processRecipe();
                smeltery.processTime = 0;
            }
        } else {
            smeltery.processTime = 0;
        }

        smeltery.processFuel();
    }

    private boolean isLit() {
        if (this.level.getBlockState(getBlockPos()).getBlock() instanceof SmelteryBlock smelteryBlock) {
            if (!smelteryBlock.validateStructure(this.level, getBlockPos(), level.getBlockState(getBlockPos())))
                return false;
        }
        return this.litTime > 0;
    }

    private void handleLitState() {
        BlockState currentState = getBlockState();
        if (currentState.getValue(SmelteryBlock.LIT) != isLit()) {
            level.setBlock(getBlockPos(), currentState.setValue(SmelteryBlock.LIT, isLit()), Block.UPDATE_ALL);
            setChanged();
        }
    }

    private void processFuel() {
        if (!(level.getBlockState(getBlockPos()).getBlock() instanceof SmelteryBlock smelteryBlock)) return;

        if (!isLit() && canProcess() && smelteryBlock.validateStructure(level, getBlockPos(), level.getBlockState(getBlockPos()))) {
            ItemStack fuelStack = inventory.getStackInSlot(11);
            if (!fuelStack.isEmpty()) {
                int burnTime = fuelStack.getBurnTime(null);
                if (burnTime > 0) {
                    fuelStack.shrink(1);
                    litDuration = burnTime;
                    litTime = burnTime;
                }
            }
        } else {
            if (litTime > 0) litTime--;
        }
    }

    private void findRecipe() {
        if (!(level.getBlockState(getBlockPos()).getBlock() instanceof SmelteryBlock)) return;

        ItemStack dieStack = this.inventory.getStackInSlot(9);

        int inputStartSlot = 0;
        int inputEndSlot = 8;

        for (int slot = inputStartSlot; slot <= inputEndSlot; slot++) {
            ItemStack inputStack = this.inventory.getStackInSlot(slot);
            if (inputStack.isEmpty()) continue;

            SmelteryRecipe recipe = SmelteryRecipe.getRecipe(this.level, inputStack, dieStack);
            if (recipe == null) continue;

            int inputCount = recipe.getIngredientCount();
            ItemStack recipeResult = recipe.getResultItem(this.level.registryAccess());

            this.totalProcessTime = recipe.getProcessTime();
            recipeInstance = new SmelteryRecipe.RecipeInstance(
                    slot,
                    inputStack.copyWithCount(inputCount),
                    dieStack.copy(),
                    recipeResult,
                    recipe.isConsumeDie(),
                    recipe.getProcessTime(),
                    recipe.getTier()
            );
            break;
        }
    }

    private boolean canProcess() {
        if (!(level.getBlockState(getBlockPos()).getBlock() instanceof SmelteryBlock smelteryBlock)) return false;

        if (recipeInstance == null) {
            findRecipe();
            return false;
        }

        int recipeSlot = recipeInstance.slot;
        ItemStack recipeInput = recipeInstance.inputStack;
        ItemStack recipeDie = recipeInstance.dieStack;
        ItemStack recipeOutput = recipeInstance.outputStack;
        this.totalProcessTime = recipeInstance.processTime;
        int recipeTier = recipeInstance.tier;

        // Check if input slot changed or is insufficient
        ItemStack inputStack = this.inventory.getStackInSlot(recipeSlot);
        if (!ItemStack.isSameItemSameComponents(inputStack, recipeInput) || inputStack.getCount() < recipeInput.getCount()) {
            recipeInstance = null;
            return false;
        }

        // Check if die slot is changed
        ItemStack dieStack = this.inventory.getStackInSlot(9);
        if (!ItemStack.isSameItemSameComponents(dieStack, recipeDie)) {
            recipeInstance = null;
            return false;
        }

        // Check output slot capacity and compatibility
        ItemStack outputStack = this.inventory.getStackInSlot(10);
        if (!outputStack.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(outputStack, recipeOutput)) {
                recipeInstance = null;
                return false;
            }

            int totalCount = outputStack.getCount() + recipeOutput.getCount();
            if (totalCount > outputStack.getMaxStackSize()) {
                recipeInstance = null;
                return false;
            }
        }

        return recipeTier <= smelteryBlock.getTier();
    }

    private void processRecipe() {
        if (!canProcess() || recipeInstance == null) return;

        int recipeSlot = recipeInstance.slot;
        int inputCount = recipeInstance.inputStack.getCount();
        ItemStack recipeOutput = recipeInstance.outputStack;
        boolean consumeDie = recipeInstance.consumeDie;

        // Shrink input and optionally consume 1 from die slot
        this.inventory.getStackInSlot(recipeSlot).shrink(inputCount);
        if (consumeDie) {
            this.inventory.getStackInSlot(9).shrink(1);
        }

        ItemStack outputStack = this.inventory.getStackInSlot(10);
        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(10, recipeOutput.copy());
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        recipeInstance = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LitTime", this.litTime);
        tag.putInt("LitDuration", this.litDuration);
        tag.putInt("ProcessTime", this.processTime);
        tag.putInt("TotalProcessTime", this.totalProcessTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        this.litTime = tag.getInt("LitTime");
        this.litDuration = tag.getInt("LitDuration");
        this.processTime = tag.getInt("ProcessTime");
        this.totalProcessTime = tag.getInt("TotalProcessTime");
    }
}