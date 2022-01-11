package io.github.orlouge.amphitritecoffer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AmphitriteCofferSlot extends Slot {
    private final PlayerEntity player;

    public AmphitriteCofferSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
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
        if (playerStack.isEmpty() || !this.canInsert(playerStack)) {
            return playerStack;
        }

        ItemStack currentStack = this.getStack();
        ItemStack convertedPlayerStack = applyConversionRecipes(playerStack, true);

        int amount = Math.min(
                Math.min(count, playerStack.getCount()),
                this.getMaxItemCount(convertedPlayerStack) - currentStack.getCount()
        );
        if (currentStack.isEmpty()) {
            convertedPlayerStack.setCount(amount);
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
        else super.setStack(applyConversionRecipes(stack, true));
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return super.getMaxItemCount(applyConversionRecipes(stack, false));
    }


    public ItemStack applyConversionRecipes(ItemStack stack, boolean giveBack) {
        Inventory dummyInventory = new SimpleInventory(new ItemStack(stack.getItem(), 1));
        Optional<AmphitriteCofferRecipe> recipeOptional = getConversionRecipe(this.player.world, dummyInventory);

        if (giveBack) {
            giveAdditionalOutputBack(player, stack.getCount(), recipeOptional);
        }

        return applyConversionRecipe(dummyInventory, stack, recipeOptional);
    }

    public static ItemStack applyConversionRecipe(ItemStack stack, Optional<AmphitriteCofferRecipe> recipeOptional) {
        return applyConversionRecipe(new SimpleInventory(new ItemStack(stack.getItem(), 1)), stack, recipeOptional);
    }

    public static ItemStack applyConversionRecipe(Inventory inventory, ItemStack stack, Optional<AmphitriteCofferRecipe> recipeOptional) {
        if (recipeOptional.isPresent()) {
            ItemStack output = recipeOptional.get().craft(inventory);
            if (output != null) {
                output.setCount(stack.getCount());

                return output;
            }
        }

        return stack.copy();
    }

    public static void giveAdditionalOutputBack(PlayerEntity player, int count, Optional<AmphitriteCofferRecipe> recipeOptional) {
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

    public static Optional<AmphitriteCofferRecipe> getConversionRecipe(World world, Inventory inventory) {
        return world.getRecipeManager().getFirstMatch(
                AmphitriteCofferRecipe.Type.INSTANCE,
                inventory,
                world
        );
    }

    public static Optional<AmphitriteCofferRecipe> getConversionRecipe(World world, ItemStack stack) {
        return getConversionRecipe(world, new SimpleInventory(new ItemStack(stack.getItem(), 1)));
    }

    public static <T, U> Optional<U> withPreConversion(
            World world,
            ItemStack stack,
            Function<ItemStack, T> action,
            Function<T, Optional<ItemStack>> getNewStack,
            BiFunction<T, Optional<ItemStack>, U> ret
    ) {
        Optional<AmphitriteCofferRecipe> conversionRecipe = AmphitriteCofferSlot.getConversionRecipe(world, stack);
        ItemStack convertedStack = AmphitriteCofferSlot.applyConversionRecipe(stack, conversionRecipe);
        int excess = Math.max(0, convertedStack.getCount() - convertedStack.getMaxCount());
        if (excess > 0) {
            convertedStack.setCount(convertedStack.getMaxCount());
        }
        int initialCount = convertedStack.getCount();
        T result = action.apply(convertedStack);
        Optional<ItemStack> newStack = getNewStack.apply(result);
        if (newStack.isPresent()) {
            convertedStack = newStack.get();
            stack.setCount(convertedStack.getCount() + excess);
            Optional<ItemStack> remainingStack = conversionRecipe
                    .flatMap(recipe -> recipe.getAdditionalOutput())
                    .map(output -> output.copy());
            return Optional.of(ret.apply(result, remainingStack));
        } else {
            return Optional.empty();
        }
    }
}
