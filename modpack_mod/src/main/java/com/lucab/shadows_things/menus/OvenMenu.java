package com.lucab.shadows_things.menus;

import com.lucab.shadows_things.block.oven.OvenBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OvenMenu extends AbstractContainerMenu {
    private final OvenBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // Costruttore Client-Side (Chiamato automaticamente da NeoForge tramite il pacchetto di rete)
    public OvenMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // Costruttore Server-Side e Principale
    public OvenMenu(int containerId, Inventory playerInventory, BlockEntity entity) {
        super(MenuRegistries.OVEN_MENU.get(), containerId);

        if (!(entity instanceof OvenBlockEntity ovenEntity)) {
            throw new IllegalStateException("BlockEntity is not an instance of OvenBlockEntity!");
        }

        this.blockEntity = ovenEntity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        // Recuperiamo l'IItemHandler della Capability di NeoForge associata alla BlockEntity
        IItemHandler itemHandler = ovenEntity.getInventoryHandler();

        // layout costanti per la GUI
        int inputX = 43;
        int inputY = 18;
        int inputSpacing = 4;

        int fuelX = 117;
        int fuelY = 38;

        int outputX = 43;
        int outputY = 60;
        int outputSpacing = 4;

        // 1. SLOTS INPUT (Indici 0, 1, 2) - Spaziati di 18 pixel l'uno dall'altro
        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i, inputX + (i * 18) + (inputSpacing * i), inputY));
        }

        // 2. SLOT FUEL (Indice 3)
        this.addSlot(new SlotItemHandler(itemHandler, 3, fuelX, fuelY));

        // 3. SLOTS OUTPUT (Indici 4, 5, 6) - Allineati sotto gli input, protetti
        for (int i = 0; i < 3; i++) {
            int outputIndex = 4 + i;
            this.addSlot(new SlotItemHandler(itemHandler, outputIndex, outputX + (i * 18) + (outputSpacing * i), outputY) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }

        // 4. Aggiunta INVENTARIO GIOCATORE
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        // 5. Aggiunta HOTBAR GIOCATORE
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161));
        }

        this.addDataSlots(ovenEntity.getContainerData());
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // Verifica la validità della distanza dal blocco per la chiusura automatica se ci si allontana
        return stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack quickmovedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            quickmovedStack = rawStack.copy();

            // Se l'indice appartiene ai primi 7 slot (0-6 dell'Oven)
            if (index < 7) {
                if (!this.moveItemStackTo(rawStack, 7, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(rawStack, quickmovedStack);
            }
            // Se l'item proviene dall'inventario del giocatore (Player Inv o Hotbar)
            else {
                Level level = this.blockEntity.getLevel();
                // Check preliminare se l'item è un carburante valido tramite la cache del livello
                boolean isFuel = level != null && rawStack.getBurnTime(null) > 0;

                if (isFuel) {
                    // Tenta prima l'inserimento nello slot Fuel (Indice 3, fine = 4 escluso)
                    if (!this.moveItemStackTo(rawStack, 3, 4, false)) {
                        // Se lo slot fuel è pieno, tenta gli slot di input (0-3 escluso -> 0,1,2)
                        if (!this.moveItemStackTo(rawStack, 0, 3, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // Se non è un carburante, invia l'item direttamente ed esclusivamente agli slot di input (0, 1, 2)
                    if (!this.moveItemStackTo(rawStack, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            // Pulizia e notifica aggiornamento dello slot gestito
            if (rawStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (rawStack.getCount() == quickmovedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, rawStack);
        }

        return quickmovedStack;
    }

    // Getter utile per sincronizzare i dati (es. progress bar) sul pacchetto dei dati della GUI (ContainerData)
    public OvenBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
