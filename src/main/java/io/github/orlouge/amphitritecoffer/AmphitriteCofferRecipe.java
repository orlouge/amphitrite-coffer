package io.github.orlouge.amphitritecoffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

public class AmphitriteCofferRecipe implements net.minecraft.recipe.Recipe<Inventory> {
    private final Ingredient input;
    private final ItemStack outputWithoutNbt;
    private final Optional<NbtCompound> outputNbt;
    private final ItemStack outputWithNbt;
    private final Optional<ItemStack> additionalOutput;
    private final Identifier id;

    public AmphitriteCofferRecipe(Ingredient input, ItemStack output, Optional<NbtCompound> outputNbt, Optional<ItemStack> additionalOutput, Identifier id) {
        this.input = input;
        this.outputWithoutNbt = output;
        this.outputWithNbt = output.copy();
        this.outputNbt = outputNbt;
        this.additionalOutput = additionalOutput;
        this.id = id;
        outputNbt.ifPresent((nbt) -> this.outputWithNbt.setNbt(nbt));
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return inventory.size() == 1 && this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return this.getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return outputWithNbt;
    }

    public ItemStack getOutputWithoutNbt() {
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

    public static class Type implements RecipeType<AmphitriteCofferRecipe> {
        public static final Type INSTANCE = new Type();
        private Type() {}
    }

    public static class JsonRecipe {
        JsonObject input;
        String output;
        JsonObject outputNbt;
        String additionalOutput;
    }

    public static class Serializer implements RecipeSerializer<AmphitriteCofferRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "amphitritecoffer:water_conversion";

        @Override
        public AmphitriteCofferRecipe read(Identifier id, JsonObject json) {
            JsonRecipe jsonRecipe = new Gson().fromJson(json, JsonRecipe.class);

            if (jsonRecipe.input == null || jsonRecipe.output == null) {
                throw new JsonSyntaxException("Invalid recipe");
            }

            Ingredient input = Ingredient.fromJson(jsonRecipe.input);
            Item output = Registry.ITEM.getOrEmpty(new Identifier(jsonRecipe.output))
                    .orElseThrow(() -> new JsonSyntaxException("No such item " + jsonRecipe.output));

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
                additionalOutput = Registry.ITEM
                        .getOrEmpty(new Identifier(jsonRecipe.additionalOutput))
                        .map(item -> new ItemStack(item, 1));

            return new AmphitriteCofferRecipe(
                    input,
                    new ItemStack(output, 1),
                    outputNbt,
                    additionalOutput,
                    id
            );
        }

        @Override
        public AmphitriteCofferRecipe read(Identifier id, PacketByteBuf buf) {
            return new AmphitriteCofferRecipe(
                    Ingredient.fromPacket(buf),
                    buf.readItemStack(),
                    buf.readOptional((buf2) -> buf2.readNbt()),
                    buf.readOptional((buf2) -> buf2.readItemStack()),
                    id
            );
        }

        @Override
        public void write(PacketByteBuf buf, AmphitriteCofferRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.outputWithoutNbt);
            buf.writeOptional(recipe.outputNbt, (buf2, nbt) -> buf2.writeNbt(nbt));
            buf.writeOptional(recipe.additionalOutput, (buf2, stack) -> buf2.writeItemStack(stack));
        }
    }
}
