package com.lucab.shadows_things.content.block.drying_rack;

import com.lucab.shadows_things.ShadowsThings;
import com.lucab.shadows_things.recipe.DryingRackRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;

public class DryingRackBlockEntity extends BlockEntity {
    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(DryingRackRegister.DRYING_RACK_BLOCK_ENTITY.get(), pos, state);
    }

    private Map<Integer, DryingRackRecipe.RecipeInstance> recipeInstance = new HashMap<>();

    private int processTime0;
    private int processTime1;
    private int processTime2;
    private int processTime3;
    private int processTime4;
    private int totalProcessTime0;
    private int totalProcessTime1;
    private int totalProcessTime2;
    private int totalProcessTime3;
    private int totalProcessTime4;

    private final ItemStackHandler inventory = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                resetProcessTime(slot);
            }
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= 0 && slot <= 4) return true;
            if (slot >= 5 && slot <= 9) return false;
            return super.isItemValid(slot, stack);
        }
    };

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> DryingRackBlockEntity.this.processTime0;
                case 1 -> DryingRackBlockEntity.this.processTime1;
                case 2 -> DryingRackBlockEntity.this.processTime2;
                case 3 -> DryingRackBlockEntity.this.processTime3;
                case 4 -> DryingRackBlockEntity.this.processTime4;
                case 5 -> DryingRackBlockEntity.this.totalProcessTime0;
                case 6 -> DryingRackBlockEntity.this.totalProcessTime1;
                case 7 -> DryingRackBlockEntity.this.totalProcessTime2;
                case 8 -> DryingRackBlockEntity.this.totalProcessTime3;
                case 9 -> DryingRackBlockEntity.this.totalProcessTime4;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> DryingRackBlockEntity.this.processTime0 = value;
                case 1 -> DryingRackBlockEntity.this.processTime1 = value;
                case 2 -> DryingRackBlockEntity.this.processTime2 = value;
                case 3 -> DryingRackBlockEntity.this.processTime3 = value;
                case 4 -> DryingRackBlockEntity.this.processTime4 = value;
                case 5 -> DryingRackBlockEntity.this.totalProcessTime0 = value;
                case 6 -> DryingRackBlockEntity.this.totalProcessTime1 = value;
                case 7 -> DryingRackBlockEntity.this.totalProcessTime2 = value;
                case 8 -> DryingRackBlockEntity.this.totalProcessTime3 = value;
                case 9 -> DryingRackBlockEntity.this.totalProcessTime4 = value;
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public ItemStackHandler getInventoryHandler() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public int getProcessTime0() {
        return containerData.get(0);
    }

    public int getProcessTime1() {
        return containerData.get(1);
    }

    public int getProcessTime2() {
        return containerData.get(2);
    }

    public int getProcessTime3() {
        return containerData.get(3);
    }

    public int getProcessTime4() {
        return containerData.get(4);
    }

    public int getTotalProcessTime0() {
        return containerData.get(5);
    }

    public int getTotalProcessTime1() {
        return containerData.get(6);
    }

    public int getTotalProcessTime2() {
        return containerData.get(7);
    }

    public int getTotalProcessTime3() {
        return containerData.get(8);
    }

    public int getTotalProcessTime4() {
        return containerData.get(9);
    }

    public void setProcessTime(int index, int value) {
        switch (index) {
            case 0 -> totalProcessTime0 = value;
            case 1 -> totalProcessTime1 = value;
            case 2 -> totalProcessTime2 = value;
            case 3 -> totalProcessTime3 = value;
            case 4 -> totalProcessTime4 = value;
        }
    }

    public int[] getProcessTime(int index) {
        return switch (index) {
            case 0 -> new int[]{getProcessTime0(), getTotalProcessTime0()};
            case 1 -> new int[]{getProcessTime1(), getTotalProcessTime1()};
            case 2 -> new int[]{getProcessTime2(), getTotalProcessTime2()};
            case 3 -> new int[]{getProcessTime3(), getTotalProcessTime3()};
            case 4 -> new int[]{getProcessTime4(), getTotalProcessTime4()};
            default -> new int[]{0, 0};
        };
    }

    private void resetProcessTime(int slot) {
        switch (slot) {
            case 0 -> processTime0 = 0;
            case 1 -> processTime1 = 0;
            case 2 -> processTime2 = 0;
            case 3 -> processTime3 = 0;
            case 4 -> processTime4 = 0;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (level.isClientSide) return;
        boolean isProcessing = false;

        for (int i = 0; i < 5; i++) {
            if (rack.canProcess(i)) {
                isProcessing = true;
                switch (i) {
                    case 0 -> rack.processTime0++;
                    case 1 -> rack.processTime1++;
                    case 2 -> rack.processTime2++;
                    case 3 -> rack.processTime3++;
                    case 4 -> rack.processTime4++;
                }

                int currentProcessTime = rack.getProcessTime(i)[0];
                int maxProcessTime = rack.getProcessTime(i)[1];

                ShadowsThings.LOGGER.info("{} | {}", currentProcessTime, maxProcessTime);

                if (currentProcessTime >= maxProcessTime) {
                    rack.processRecipe(i);
                    rack.resetProcessTime(i);
                }
            } else {
                rack.resetProcessTime(i);
            }
        }

        if (isProcessing) {
            rack.setChanged();
        }
    }

    private void findRecipe(int slot) {
        ItemStack inputStack = this.inventory.getStackInSlot(slot);
        if (inputStack.isEmpty()) return;

        DryingRackRecipe recipe = DryingRackRecipe.getRecipe(level, inputStack);
        if (recipe == null) return;

        int countNeeded = recipe.getIngredient().count();
        if (inputStack.getCount() < countNeeded) return;

        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());
        int totalProcessTime = recipe.getProcessTime();

        recipeInstance.put(slot,
                new DryingRackRecipe.RecipeInstance(
                        inputStack.copyWithCount(countNeeded),
                        resultStack,
                        totalProcessTime
                )
        );
    }

    private boolean canProcess(int slot) {
        if (!recipeInstance.containsKey(slot)) {
            findRecipe(slot);
            return false;
        }

        DryingRackRecipe.RecipeInstance instance = recipeInstance.get(slot);
        ItemStack recipeInput = instance.inputStack();
        ItemStack recipeOutput = instance.outputStack();
        int totalProcessTime = instance.processTime();
        setProcessTime(slot, totalProcessTime);

        // Check if input slot changed or is insufficient
        ItemStack inputStack = this.inventory.getStackInSlot(slot);
        if (!ItemStack.isSameItemSameComponents(inputStack, recipeInput) || inputStack.getCount() < recipeInput.getCount()) {
            recipeInstance.remove(slot);
            return false;
        }

        // Check output slot capacity and compatibility
        ItemStack outputStack = this.inventory.getStackInSlot(slot + 5);
        if (!outputStack.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(outputStack, recipeOutput)) {
                recipeInstance.remove(slot);
                return false;
            }

            int totalCount = outputStack.getCount() + recipeOutput.getCount();
            if (totalCount > outputStack.getMaxStackSize()) {
                recipeInstance.remove(slot);
                return false;
            }
        }

        return true;
    }

    private void processRecipe(int slot) {
        if (!canProcess(slot) || !recipeInstance.containsKey(slot)) return;

        DryingRackRecipe.RecipeInstance instance = recipeInstance.get(slot);
        int countNeeded = instance.inputStack().getCount();
        ItemStack recipeOutput = recipeInstance.get(slot).outputStack();

        this.inventory.getStackInSlot(slot).shrink(countNeeded);

        ItemStack outputStack = this.inventory.getStackInSlot(slot + 5);
        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(slot + 5, recipeOutput.copy());
        } else {
            outputStack.grow(recipeOutput.getCount());
        }

        recipeInstance.remove(slot);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Save inventory
        tag.put("Inventory", inventory.serializeNBT(registries));

        // Save process times
        tag.putInt("ProcessTime0", processTime0);
        tag.putInt("ProcessTime1", processTime1);
        tag.putInt("ProcessTime2", processTime2);
        tag.putInt("ProcessTime3", processTime3);
        tag.putInt("ProcessTime4", processTime4);
        tag.putInt("TotalProcessTime0", totalProcessTime0);
        tag.putInt("TotalProcessTime1", totalProcessTime1);
        tag.putInt("TotalProcessTime2", totalProcessTime2);
        tag.putInt("TotalProcessTime3", totalProcessTime3);
        tag.putInt("TotalProcessTime4", totalProcessTime4);

        // Save recipe instances
        CompoundTag recipesTag = new CompoundTag();
        recipeInstance.forEach((slot, instance) -> {
            CompoundTag instanceTag = new CompoundTag();
            instanceTag.put("InputStack", instance.inputStack().save(registries, new CompoundTag()));
            instanceTag.put("OutputStack", instance.outputStack().save(registries, new CompoundTag()));
            instanceTag.putInt("ProcessTime", instance.processTime());
            recipesTag.put(String.valueOf(slot), instanceTag);
        });
        tag.put("RecipeInstances", recipesTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Load inventory
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));

        // Load process times
        processTime0 = tag.getInt("ProcessTime0");
        processTime1 = tag.getInt("ProcessTime1");
        processTime2 = tag.getInt("ProcessTime2");
        processTime3 = tag.getInt("ProcessTime3");
        processTime4 = tag.getInt("ProcessTime4");
        totalProcessTime0 = tag.getInt("TotalProcessTime0");
        totalProcessTime1 = tag.getInt("TotalProcessTime1");
        totalProcessTime2 = tag.getInt("TotalProcessTime2");
        totalProcessTime3 = tag.getInt("TotalProcessTime3");
        totalProcessTime4 = tag.getInt("TotalProcessTime4");

        // Load recipe instances
        recipeInstance.clear();
        if (tag.contains("RecipeInstances")) {
            CompoundTag recipesTag = tag.getCompound("RecipeInstances");
            for (String key : recipesTag.getAllKeys()) {
                int slot = Integer.parseInt(key);
                CompoundTag instanceTag = recipesTag.getCompound(key);

                ItemStack input = ItemStack.parse(registries, instanceTag.getCompound("InputStack")).orElse(ItemStack.EMPTY);
                ItemStack output = ItemStack.parse(registries, instanceTag.getCompound("OutputStack")).orElse(ItemStack.EMPTY);
                int time = instanceTag.getInt("ProcessTime");

                recipeInstance.put(slot, new DryingRackRecipe.RecipeInstance(input, output, time));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }
}
