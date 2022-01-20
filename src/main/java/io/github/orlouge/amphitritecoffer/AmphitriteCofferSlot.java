package io.github.orlouge.amphitritecoffer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

public class AmphitriteCofferSlot extends Slot {
    private final PlayerEntity player;
    private final PropertyDelegate propertyDelegate;

    public AmphitriteCofferSlot(PlayerEntity player, Inventory inventory, PropertyDelegate propertyDelegate, int x, int y, int index) {
        super(inventory, index, x, y);
        this.player = player;
        this.propertyDelegate = propertyDelegate;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (AmphitriteCofferMod.CONFIG.allowShulkersInCoffers) {
            return !(stack.getItem() instanceof BlockItem &&
                     ((BlockItem) stack.getItem()).getBlock() instanceof AmphitriteCofferBlock);
        } else {
            return stack.getItem().canBeNested();
        }
    }

    @Override
    public ItemStack insertStack(ItemStack playerStack, int count) {
        if (this.player.world.isClient()) super.insertStack(playerStack, count);

        if (playerStack.isEmpty() || !this.canInsert(playerStack)) {
            return playerStack;
        }

        ItemStack currentStack = this.getStack();
        ItemStack convertedPlayerStack = convertAndConsume(
                playerStack,
                false,
                maxStack -> Math.min(maxStack - currentStack.getCount(), count)
        );

        int amount = convertedPlayerStack.getCount();
        if (currentStack.isEmpty()) {
            playerStack.decrement(amount);
            this.setStack(convertedPlayerStack);
        } else if (ItemStack.canCombine(currentStack, convertedPlayerStack)) {
            playerStack.decrement(amount);
            currentStack.increment(amount);
            this.setStack(currentStack);
        }
        return playerStack;
    }

    @Override
    public void setStack(ItemStack stack) {
        if (this.player.world.isClient()) super.setStack(stack);
        else super.setStack(convertAndConsume(stack, false, max -> max));
        // XXX: causes double conversions when shift-clicking, but is required
        // to apply conversions when swapping items
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return super.getMaxItemCount(convertAndConsume(stack, true, max -> max));
    }


    public ItemStack convertAndConsume(ItemStack stack, boolean dryRun, Function<Integer, Integer> maxFunction) {
        Inventory dummyInventory = dummyInventory(stack);
        Optional<WaterConversionRecipe> recipeOptional = getConversionRecipe(this.player.world, dummyInventory);

        ItemStack convertedStack = applyConversionRecipe(dummyInventory, stack, this.propertyDelegate, recipeOptional, maxFunction);

        if (!dryRun) {
            giveAdditionalOutputBack(player, convertedStack.getCount(), recipeOptional);
            consumeCharge(this.propertyDelegate, convertedStack.getCount(), recipeOptional);
        }

        return convertedStack;
    }

    public static ItemStack applyConversionRecipe(ItemStack stack, PropertyDelegate propertyDelegate, Optional<WaterConversionRecipe> recipeOptional) {
        return applyConversionRecipe(dummyInventory(stack), stack, propertyDelegate, recipeOptional, max -> max);
    }

    private static Inventory dummyInventory(ItemStack stack) {
        ItemStack dummyStack = stack.copy();
        dummyStack.setCount(1);
        return new SimpleInventory(dummyStack);
    }

    public static ItemStack applyConversionRecipe(Inventory inventory, ItemStack stack, PropertyDelegate propertyDelegate, Optional<WaterConversionRecipe> recipeOptional, Function<Integer, Integer> maxStack) {
        if (recipeOptional.isPresent()) {
            ItemStack output = recipeOptional.get().craft(inventory);
            if (output != null) {
                int count = Math.min(stack.getCount(), maxStack.apply(output.getMaxCount()));
                count = consumeCharge(propertyDelegate, count, recipeOptional.get().getCost(), true);
                if (count > 0) {
                    output.setCount(count);
                    return output;
                }
            }
        }

        ItemStack output = stack.copy();
        int count = Math.min(stack.getCount(), maxStack.apply(stack.getMaxCount()));
        output.setCount(count);
        return output;
    }

    public static void giveAdditionalOutputBack(PlayerEntity player, int count, Optional<WaterConversionRecipe> recipeOptional) {
        recipeOptional.flatMap(recipe -> recipe.getAdditionalOutput()).ifPresent(baseStack -> {
            ItemStack additionalOutputStack = baseStack.copy();
            additionalOutputStack.setCount(count);
            giveAdditionalOutputBack(player, additionalOutputStack);
        });
    }


    public static void giveAdditionalOutputBack(PlayerEntity player, ItemStack remainingStack) {
        if (player.world.isClient()) return;
        if (player.getInventory().insertStack(remainingStack) && remainingStack.isEmpty()) {
            player.currentScreenHandler.sendContentUpdates();
        } else {
            player.dropItem(remainingStack, false);
        }
    }

    public static Optional<WaterConversionRecipe> getConversionRecipe(World world, Inventory inventory) {
        return world.getRecipeManager().getFirstMatch(
                WaterConversionRecipe.Type.INSTANCE,
                inventory,
                world
        );
    }

    public static Optional<WaterConversionRecipe> getConversionRecipe(World world, ItemStack stack) {
        return getConversionRecipe(world, dummyInventory(stack));
    }

    public static int consumeCharge(PropertyDelegate propertyDelegate, int count, Optional<WaterConversionRecipe> optional) {
        return optional.map(recipe -> consumeCharge(propertyDelegate, count, recipe.getCost(), false)).orElse(count);
    }

    public static int consumeCharge(PropertyDelegate propertyDelegate, int count, int cost, boolean dryRun) {
        if (cost <= 0) return count;

        int charge = propertyDelegate.get(0);
        int effectiveCount = Math.min(count, charge / cost);
        if (effectiveCount <= 0) {
            propertyDelegate.set(0, - count * cost);
            charge = propertyDelegate.get(0);
            effectiveCount = Math.min(count, charge / cost);
        }
        if (!dryRun) {
            propertyDelegate.set(0, charge - cost * effectiveCount);
        }
        return effectiveCount;
    }

    /**
     * Wraps an operation that transfers a stack consuming it, making it
     * use a converted stack instead of the original one and applying
     * the changes to the latter at the end of the operation.
     */
    public interface Transfer {
        default int maxAmount(int maxStack) {
            return maxStack;
        }

        Optional<ItemStack> transferWithProxy(ItemStack convertedProxyStack);
    }

    public static class TransferResult {
        public final ItemStack transferResult;
        public final ItemStack additionalOutput;

        public TransferResult(ItemStack transferResult, ItemStack additionalOutput) {
            this.transferResult = transferResult;
            this.additionalOutput = additionalOutput;
        }
    }

    // XXX: called from client, causes desync when the charge is too low for the recipe
    public static Optional<TransferResult> transfer(World world, PropertyDelegate propertyDelegate, ItemStack stack, Transfer transfer) {
        Optional<WaterConversionRecipe> conversionRecipe = AmphitriteCofferSlot.getConversionRecipe(world, stack);
        ItemStack convertedStack = AmphitriteCofferSlot.applyConversionRecipe(stack, propertyDelegate, conversionRecipe);
        int excess = stack.getCount() - convertedStack.getCount();
        int initialCount = convertedStack.getCount();

        Optional<ItemStack> newStack = transfer.transferWithProxy(convertedStack);
        if (newStack.isPresent()) {
            convertedStack = newStack.get();
            int amount = initialCount - convertedStack.getCount();
            stack.setCount(convertedStack.getCount() + excess);
            ItemStack remainingStack = conversionRecipe
                    .flatMap(recipe -> recipe.getAdditionalOutput())
                    .map(output -> { ItemStack rem = output.copy(); rem.setCount(amount); return rem;})
                    .orElse(ItemStack.EMPTY);
            consumeCharge(propertyDelegate, amount, conversionRecipe);
            return Optional.of(new TransferResult(convertedStack, remainingStack));
        } else {
            return Optional.empty();
        }
    }
}
