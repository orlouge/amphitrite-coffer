package io.github.orlouge.amphitritecoffer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class AmphitriteCofferScreenHandler
extends ScreenHandler implements AmphitriteCofferSlot.Transfer {
    private final Inventory inventory;
    private final Inventory chargeInventory;
    private PropertyDelegate propertyDelegate;

    public AmphitriteCofferScreenHandler(int syncId, PlayerInventory playerInventory) {
            this(syncId, playerInventory, new SimpleInventory(18), new SimpleInventory(1), new ArrayPropertyDelegate(1));
    }

    public AmphitriteCofferScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, Inventory chargeInventory, PropertyDelegate propertyDelegate) {
            super(AmphitriteCofferMod.AMPHITRITE_COFFER_SCREEN_HANDLER, syncId);
            AmphitriteCofferScreenHandler.checkSize(inventory, 18);
            AmphitriteCofferScreenHandler.checkSize(chargeInventory, 1);
            this.inventory = inventory;
            this.chargeInventory = chargeInventory;
            this.propertyDelegate = propertyDelegate;
            inventory.onOpen(playerInventory.player);
            chargeInventory.onOpen(playerInventory.player);
            this.addProperties(propertyDelegate);
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 9; ++k) {
                    this.addSlot(new AmphitriteCofferSlot(playerInventory.player, inventory, propertyDelegate, 8 + k * 18, 39 + j * 18, k + j * 9));
                }
            }
            // this.addSlot(new ChargeSlot(inventory, inventory.size() - 1, 80, 16));
            this.addSlot(new ChargeSlot(chargeInventory, 0, 80, 16));
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 89 + j * 18));
                }
            }
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 147));
            }
        }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack giveBack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack transferringStack = slot.getStack();
            itemStack = transferringStack.copy();

            if (index < this.inventory.size() && !this.insertItem(transferringStack, this.inventory.size(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            } else {
                Optional<AmphitriteCofferSlot.TransferResult> transferred =
                        AmphitriteCofferSlot.transfer(player.world, this.propertyDelegate, transferringStack, this);
                if (transferred.isPresent()) giveBack = transferred.get().additionalOutput;
                else return ItemStack.EMPTY;
            }
            if (transferringStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        if (!giveBack.isEmpty()) {
            AmphitriteCofferSlot.giveAdditionalOutputBack(player, giveBack);
        }
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
        this.chargeInventory.onClose(player);
    }

    public int getCharge() {
        return this.propertyDelegate.get(0);
    }

    @Override
    public Optional<ItemStack> transferWithProxy(ItemStack convertedStack) {
        return this.insertItem(convertedStack, 0, this.inventory.size(), false) ? Optional.of(convertedStack) : Optional.<ItemStack>empty();
    }

    static class ChargeSlot extends Slot {
        public ChargeSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.HEART_OF_THE_SEA);
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }
    }
}
