package io.github.orlouge.amphitritecoffer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class AmphitriteCofferScreenHandler
extends ScreenHandler {
    private final Inventory inventory;

    public AmphitriteCofferScreenHandler(int syncId, PlayerInventory playerInventory) {
            this(syncId, playerInventory, new SimpleInventory(18));
        }

    public AmphitriteCofferScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
            super(AmphitriteCofferMod.AMPHITRITE_COFFER_SCREEN_HANDLER, syncId);
            AmphitriteCofferScreenHandler.checkSize(inventory, 18);
            this.inventory = inventory;
            inventory.onOpen(playerInventory.player);
            int i = -36;
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 9; ++k) {
                    this.addSlot(new AmphitriteCofferSlot(playerInventory.player, inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
                }
            }
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
                }
            }
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 161 + i));
            }
        }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<ItemStack> giveBack = Optional.empty();
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack transferringStack = slot.getStack();
            itemStack = transferringStack.copy();

            if (index < this.inventory.size() && !this.insertItem(transferringStack, this.inventory.size(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            } else {
                Optional<Optional<ItemStack>> transferred = AmphitriteCofferSlot.withPreConversion(player.world, transferringStack,
                        convertedStack -> this.insertItem(convertedStack, 0, this.inventory.size(), false) ? Optional.of(convertedStack) : Optional.<ItemStack>empty(),
                        convertedStack -> convertedStack,
                        (convertedStack, remaining) -> remaining
                );
                if (transferred.isPresent()) giveBack = transferred.get();
                else return ItemStack.EMPTY;
            }
            if (transferringStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        giveBack.ifPresent(remainingStack ->
                AmphitriteCofferSlot.giveAdditionalOutputBack(player, remainingStack)
        );
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }
}
