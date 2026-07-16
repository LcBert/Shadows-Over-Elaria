package com.lucab.shadows_things.block.oven;

import com.lucab.shadows_things.block.BlocksRegister;
import com.lucab.shadows_things.recipe.OvenRecipe;
import com.lucab.shadows_things.recipe.RecipesRegistries;
import com.lucab.shadows_things.recipe.SingleItemRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

public class OvenBlockEntity extends BlockEntity {
    public OvenBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksRegister.OVEN_BLOCK_ENTITY.get(), pos, state);
    }

    private int litTime;
    private int litDuration;
    private int cookTime0;
    private int cookTime1;
    private int cookTime2;
    private int cookingTotalTime0 = 200;
    private int cookingTotalTime1 = 200;
    private int cookingTotalTime2 = 200;

    public final ItemStackHandler inventory = new ItemStackHandler(7) {
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // Output slots, cannot insert items
            if (slot >= 4 && slot <= 6) return false;

            // Fuel Slot, only items with fuel properties
            if (slot == 3) return stack.getBurnTime(null) > 0;

            return super.isItemValid(slot, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot >= 4 && slot <= 6) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    };

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> OvenBlockEntity.this.litTime;
                case 1 -> OvenBlockEntity.this.litDuration;
                case 2 -> OvenBlockEntity.this.cookTime0;
                case 3 -> OvenBlockEntity.this.cookTime1;
                case 4 -> OvenBlockEntity.this.cookTime2;
                case 5 -> OvenBlockEntity.this.cookingTotalTime0;
                case 6 -> OvenBlockEntity.this.cookingTotalTime1;
                case 7 -> OvenBlockEntity.this.cookingTotalTime2;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> OvenBlockEntity.this.litTime = value;
                case 1 -> OvenBlockEntity.this.litDuration = value;
                case 2 -> OvenBlockEntity.this.cookTime0 = value;
                case 3 -> OvenBlockEntity.this.cookTime1 = value;
                case 4 -> OvenBlockEntity.this.cookTime2 = value;
                case 5 -> OvenBlockEntity.this.cookingTotalTime0 = value;
                case 6 -> OvenBlockEntity.this.cookingTotalTime1 = value;
                case 7 -> OvenBlockEntity.this.cookingTotalTime2 = value;
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public IItemHandler getInventoryHandler() {
        return this.inventory;
    }

    public ContainerData getContainerData() {
        return this.containerData;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OvenBlockEntity oven) {
        if (level.isClientSide) return;

        // Handle Lit State
        oven.handleLitState();

        // Handle Process
        if (oven.canProcessRow(0)) {
            if (oven.isLit()) oven.cookTime0++;
            else if (level.getGameTime() % 20 == 0 && oven.cookTime0 > 0) oven.cookTime0--;

            if (oven.cookTime0 >= oven.cookingTotalTime0) {
                oven.processRecipe(0);
                oven.cookTime0 = 0;
            }
        } else {
            oven.cookTime0 = 0;
        }

        if (oven.canProcessRow(1)) {
            if (oven.isLit()) oven.cookTime1++;
            else if (level.getGameTime() % 20 == 0 && oven.cookTime1 > 0) oven.cookTime1--;

            if (oven.cookTime1 >= oven.cookingTotalTime1) {
                oven.processRecipe(1);
                oven.cookTime1 = 0;
            }
        } else {
            oven.cookTime1 = 0;
        }

        if (oven.canProcessRow(2)) {
            if (oven.isLit()) oven.cookTime2++;
            else if (level.getGameTime() % 20 == 0 && oven.cookTime2 > 0) oven.cookTime2--;

            if (oven.cookTime2 >= oven.cookingTotalTime2) {
                oven.processRecipe(2);
                oven.cookTime2 = 0;
            }
        } else {
            oven.cookTime2 = 0;
        }

        // Process fuel
        oven.processFuel();
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    private void handleLitState() {
        BlockState currentState = getBlockState();

        if (currentState.getValue(OvenBlock.LIT) != isLit()) {
            level.setBlock(getBlockPos(), currentState.setValue(OvenBlock.LIT, isLit()), Block.UPDATE_ALL);
            setChanged();
        }
    }

    private void processFuel() {
        if (!isLit() && (canProcessRow(0) || canProcessRow(1) || canProcessRow(2))) {
            ItemStack fuelStack = inventory.getStackInSlot(3);
            if (!fuelStack.isEmpty()) {
                int burnTime = fuelStack.getBurnTime(null);
                if (burnTime > 0) {
                    fuelStack.shrink(1);
                    litDuration = burnTime;
                    litTime = burnTime;
                }
            }
        } else {
            litTime--;
        }
    }

    private boolean hasItems() {
        for (int i = 0; i < 3; i++) if (!this.inventory.getStackInSlot(i).isEmpty()) return true;
        return false;
    }

    private boolean canProcessRow(int row) {
        int inputSlot = -1, outputSlot = -1;
        switch (row) {
            case 0 -> {
                inputSlot = 0;
                outputSlot = 4;
            }
            case 1 -> {
                inputSlot = 1;
                outputSlot = 5;
            }
            case 2 -> {
                inputSlot = 2;
                outputSlot = 6;
            }
        }
        if (inputSlot == -1 || outputSlot == -1) return false;

        ItemStack inputStack = this.inventory.getStackInSlot(inputSlot);
        if (inputStack.isEmpty()) return false;

        SingleItemRecipeInput inputWrapper = new SingleItemRecipeInput(inputStack);

        Optional<RecipeHolder<OvenRecipe>> match = this.level.getRecipeManager().getRecipeFor(RecipesRegistries.OVEN_TYPE.get(), inputWrapper, level);

        if (match.isEmpty()) return false;

        OvenRecipe recipe = match.get().value();
        ItemStack recipeResult = recipe.getResultItem(this.level.registryAccess());

        switch (row) {
            case 0 -> this.cookingTotalTime0 = recipe.getCookingTime();
            case 1 -> this.cookingTotalTime1 = recipe.getCookingTime();
            case 2 -> this.cookingTotalTime2 = recipe.getCookingTime();
        }

        ItemStack outputStack = this.inventory.getStackInSlot(outputSlot);
        if (outputStack.isEmpty()) return true;

        if (!ItemStack.isSameItemSameComponents(outputStack, recipeResult)) return false;

        return outputStack.getCount() + recipeResult.getCount() <= outputStack.getMaxStackSize();
    }

    private void processRecipe(int row) {
        int inputSlot = -1, outputSlot = -1;
        switch (row) {
            case 0 -> {
                inputSlot = 0;
                outputSlot = 4;
            }
            case 1 -> {
                inputSlot = 1;
                outputSlot = 5;
            }
            case 2 -> {
                inputSlot = 2;
                outputSlot = 6;
            }
        }
        if (inputSlot == -1 || outputSlot == -1) return;

        if (!canProcessRow(row)) return;

        ItemStack inputStack = this.inventory.getStackInSlot(inputSlot);
        SingleItemRecipeInput inputWrapper = new SingleItemRecipeInput(inputStack);

        OvenRecipe recipe = this.level.getRecipeManager().getRecipeFor(RecipesRegistries.OVEN_TYPE.get(), inputWrapper, this.level).map(RecipeHolder::value).orElse(null);

        if (recipe == null) return;

        ItemStack recipeResult = recipe.getResultItem(this.level.registryAccess());
        ItemStack outputStack = this.inventory.getStackInSlot(outputSlot);

        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(outputSlot, recipeResult.copy());
        } else {
            outputStack.grow(recipeResult.getCount());
        }

        inputStack.shrink(recipe.getIngredientCount());
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LitTime", this.litTime);
        tag.putInt("LitDuration", this.litDuration);
        tag.putInt("CookTime0", this.cookTime0);
        tag.putInt("CookTime1", this.cookTime1);
        tag.putInt("CookTime2", this.cookTime2);
        tag.putInt("CookingTotalTime0", this.cookingTotalTime0);
        tag.putInt("CookingTotalTime1", this.cookingTotalTime1);
        tag.putInt("CookingTotalTime2", this.cookingTotalTime2);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        this.litTime = tag.getInt("LitTime");
        this.litDuration = tag.getInt("LitDuration");
        this.cookTime0 = tag.getInt("CookTime0");
        this.cookTime1 = tag.getInt("CookTime1");
        this.cookTime2 = tag.getInt("CookTime2");
        this.cookingTotalTime0 = tag.getInt("CookingTotalTime0");
        this.cookingTotalTime1 = tag.getInt("CookingTotalTime1");
        this.cookingTotalTime2 = tag.getInt("CookingTotalTime2");
    }
}
