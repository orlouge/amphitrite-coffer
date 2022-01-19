package io.github.orlouge.amphitritecoffer.mixin;

import io.github.orlouge.amphitritecoffer.AmphitriteCofferBlockEntity;
import io.github.orlouge.amphitritecoffer.AmphitriteCofferSlot;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Shadow
    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        throw new RuntimeException("wtf");
    }

    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void onTransfer(Inventory from, Inventory to, ItemStack stack, int slot, Direction side, CallbackInfoReturnable<ItemStack> cir) {
        if (to instanceof AmphitriteCofferBlockEntity) {
            AmphitriteCofferBlockEntity coffer = (AmphitriteCofferBlockEntity) to;
            if (!coffer.isConverting()) return;
            coffer.setShouldConvert(false);
            AmphitriteCofferSlot.Transfer transfer =
                    convertedStack -> Optional.of(transfer(from, to, convertedStack, slot, side));
            Optional<AmphitriteCofferSlot.TransferResult> result =
                    AmphitriteCofferSlot.transfer(coffer.getWorld(), coffer.getPropertyDelegate(), stack, transfer);
            Optional<ItemStack> newStack = result.map(res -> res.transferResult);
            result.ifPresent(res -> {
                if (!res.additionalOutput.isEmpty()) {
                    ItemStack droppedStack = HopperBlockEntity.transfer(
                            new SimpleInventory(res.additionalOutput), to, res.additionalOutput, Direction.UP
                    );
                    if (!droppedStack.isEmpty()) {
                        coffer.getWorld().spawnEntity(coffer.dropStack(droppedStack));
                    }
                }
            });
            coffer.setShouldConvert(true);

            cir.setReturnValue(newStack.orElse(null));
            cir.cancel();
        }
    }
}
