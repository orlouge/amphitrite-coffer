package io.github.orlouge.amphitritecoffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class WaterConversionRecipe implements net.minecraft.recipe.Recipe<Inventory> {
    private final Ingredient input;
    private final Optional<NbtCompound> inputRequiredNbt;
    private final Optional<ItemStack> outputWithoutNbt;
    private final Optional<NbtCompound> outputNbt;
    private final Optional<ItemStack> outputWithNbt;
    private final Optional<ItemStack> additionalOutput;
    private final Optional<Identifier> enchantment;
    private final ItemStack displayOutput;
    private final int enchantmentLevel;
    private final int cost;
    private final Identifier id;

    public WaterConversionRecipe(Ingredient input, Optional<NbtCompound> inputRequiredNbt, Optional<ItemStack> output, Optional<NbtCompound> outputNbt, Optional<ItemStack> additionalOutput, Optional<Identifier> enchantment, int enchantmentLevel, int cost, Identifier id) {
        this.input = input;
        this.inputRequiredNbt = inputRequiredNbt;
        this.outputWithoutNbt = output;
        this.outputWithNbt = output.map(out -> out.copy());
        this.outputNbt = outputNbt;
        this.additionalOutput = additionalOutput;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.cost = cost;
        this.id = id;
        outputNbt.ifPresent((nbt) -> this.outputWithNbt.ifPresent(out -> out.setNbt(nbt)));
        this.displayOutput = this.craft(new SimpleInventory(getInput().iterator().next()));
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (!(inventory.size() == 1 && this.input.test(inventory.getStack(0)))) return false;
        boolean hasRequiredNbt = this.inputRequiredNbt.map(required -> {
            return inventory.getStack(0).getOrCreateNbt().equals(required);
        }).orElse(true);
        if (!hasRequiredNbt) return false;
        boolean alreadyHasEnchantment = this.enchantment.map(enchantment -> {
            NbtList currentEnchantments = inventory.getStack(0).getEnchantments();
            for (int i = 0; i < currentEnchantments.size(); i++) {
                NbtCompound currentEnchantment = currentEnchantments.getCompound(i);
                if (EnchantmentHelper.getIdFromNbt(currentEnchantment).equals(enchantment) &&
                    EnchantmentHelper.getLevelFromNbt(currentEnchantment) >= enchantmentLevel) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
        return !alreadyHasEnchantment;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack output = this.outputWithNbt.orElse(inventory.getStack(0)).copy();
        this.enchantment.ifPresent(enchantment -> {
            if (enchantmentLevel < 1) return;
            int currentIndex = -1, currentLevel = 0;
            NbtList currentEnchantments;

            NbtCompound outputNbt = output.getOrCreateNbt();
            if (!outputNbt.contains(output.ENCHANTMENTS_KEY, 9)) {
                outputNbt.put(output.ENCHANTMENTS_KEY, new NbtList());
                currentEnchantments = output.getEnchantments();
            } else {
                currentEnchantments = output.getEnchantments();
                for (int i = 0; i < currentEnchantments.size(); i++) {
                    NbtCompound currentEnchantment = currentEnchantments.getCompound(i);
                    Identifier currentId = EnchantmentHelper.getIdFromNbt(currentEnchantment);
                    if (currentId.equals(enchantment)) {
                        currentLevel = EnchantmentHelper.getLevelFromNbt(currentEnchantment);
                        currentIndex = i;
                        break;
                    }
                }
            }
            if (currentIndex >= 0) {
                currentEnchantments.remove(currentIndex);
            }
            currentEnchantments.add(EnchantmentHelper.createNbt(enchantment, enchantmentLevel));
        });
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    public Collection<ItemStack> getInput() {
        return Arrays.stream(input.getMatchingStacks())
                .map(stack -> {
                    ItemStack newStack = stack.copy();
                    inputRequiredNbt.ifPresent(nbt -> newStack.setNbt(nbt));
                    return newStack;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack getOutput() {
        return outputWithNbt.orElse(ItemStack.EMPTY);
    }

    public ItemStack getSampleOutput() {
        return displayOutput;
    }

    public Optional<ItemStack> getOutputWithoutNbt() {
        return outputWithoutNbt;
    }

    public Optional<ItemStack> getAdditionalOutput() {
        return this.additionalOutput;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public int getCost() { return cost; }

    public static class Type implements RecipeType<WaterConversionRecipe> {
        public static final Type INSTANCE = new Type();
        private Type() {}
    }

    public static class JsonRecipe {
        JsonObject input;
        JsonObject inputRequiredNbt;
        String output;
        JsonObject outputNbt;
        String additionalOutput;
        String enchantment;
        int enchantmentLevel;
        int cost;
    }

    public static class Serializer implements RecipeSerializer<WaterConversionRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "amphitritecoffer:water_conversion";

        @Override
        public WaterConversionRecipe read(Identifier id, JsonObject json) {
            JsonRecipe jsonRecipe = new Gson().fromJson(json, JsonRecipe.class);

            if (jsonRecipe.input == null || (jsonRecipe.output == null && jsonRecipe.enchantment == null)) {
                throw new JsonSyntaxException("Invalid recipe");
            }

            Ingredient input = Ingredient.fromJson(jsonRecipe.input);
            Optional<Item> output = Optional.empty();
            if (jsonRecipe.output != null) {
                output = Optional.of(Registries.ITEM.getOrEmpty(new Identifier(jsonRecipe.output))
                        .orElseThrow(() -> new JsonSyntaxException("No such item " + jsonRecipe.output)));
            }

            Optional<Identifier> enchantment = Optional.empty();
            int enchantmentLevel = 0;
            if (jsonRecipe.enchantment != null && jsonRecipe.enchantment.length() > 0) {
                enchantment = Optional.of(new Identifier(jsonRecipe.enchantment));
                enchantmentLevel = jsonRecipe.enchantmentLevel;
            }

            Optional<NbtCompound> inputRequiredNbt = Optional.empty();
            if (jsonRecipe.inputRequiredNbt != null) {
                StringNbtReader reader = new StringNbtReader(
                        new StringReader(jsonRecipe.inputRequiredNbt.toString())
                );
                try {
                    inputRequiredNbt = Optional.of(reader.parseCompound());
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }

            Optional<NbtCompound> outputNbt = Optional.empty();
            if (jsonRecipe.outputNbt != null) {
                StringNbtReader reader = new StringNbtReader(
                        new StringReader(jsonRecipe.outputNbt.toString())
                );
                try {
                    outputNbt = Optional.of(reader.parseCompound());
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }

            Optional<ItemStack> additionalOutput = Optional.empty();
            if (jsonRecipe.additionalOutput != null)
                additionalOutput = Registries.ITEM
                        .getOrEmpty(new Identifier(jsonRecipe.additionalOutput))
                        .map(item -> new ItemStack(item, 1));

            return new WaterConversionRecipe(
                    input,
                    inputRequiredNbt,
                    output.map(item -> new ItemStack(item, 1)),
                    outputNbt,
                    additionalOutput,
                    enchantment,
                    enchantmentLevel,
                    jsonRecipe.cost,
                    id
            );
        }

        @Override
        public WaterConversionRecipe read(Identifier id, PacketByteBuf buf) {
            return new WaterConversionRecipe(
                    Ingredient.fromPacket(buf),
                    buf.readOptional((buf2) -> buf2.readNbt()),
                    buf.readOptional((buf2) -> buf2.readItemStack()),
                    buf.readOptional((buf2) -> buf2.readNbt()),
                    buf.readOptional((buf2) -> buf2.readItemStack()),
                    buf.readOptional((buf2) -> buf2.readIdentifier()),
                    buf.readInt(),
                    buf.readInt(),
                    id
            );
        }

        @Override
        public void write(PacketByteBuf buf, WaterConversionRecipe recipe) {
            recipe.input.write(buf);
            buf.writeOptional(recipe.inputRequiredNbt, (buf2, nbt) -> buf2.writeNbt(nbt));
            buf.writeOptional(recipe.outputWithoutNbt, (buf2, stack) -> buf2.writeItemStack(stack));
            buf.writeOptional(recipe.outputNbt, (buf2, nbt) -> buf2.writeNbt(nbt));
            buf.writeOptional(recipe.additionalOutput, (buf2, stack) -> buf2.writeItemStack(stack));
            buf.writeOptional(recipe.enchantment, (buf2, ench) -> buf2.writeIdentifier(ench));
            buf.writeInt(recipe.enchantmentLevel);
            buf.writeInt(recipe.cost);
        }
    }
}
