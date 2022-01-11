package io.github.orlouge.amphitritecoffer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;


public class AmphitriteCofferBlockEntity extends LootableContainerBlockEntity implements SidedInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(18, ItemStack.EMPTY);
    private boolean shouldConvert = true;

    public AmphitriteCofferBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK_ENTITY, blockPos, blockState);
    }

    public static void tick(World world1, BlockPos pos, BlockState state1, AmphitriteCofferBlockEntity be) {
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public void onOpen(PlayerEntity player) {
        this.world.emitGameEvent((Entity) player, GameEvent.CONTAINER_OPEN, this.pos);
        this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public void onClose(PlayerEntity player) {
        this.world.emitGameEvent((Entity) player, GameEvent.CONTAINER_CLOSE, this.pos);
        this.world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
    }

    @Override
    protected Text getContainerName() {
        return Text.of("Amphitrite Coffer");
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt) && nbt.contains("Items", 9)) {
            Inventories.readNbt(nbt, this.inventory);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, false);
        }
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return IntStream.range(0, 18).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.world.getBlockState(this.pos).get(Properties.WATERLOGGED) &&
               stack.getCount() <= 1 &&
               !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) &&
               !(Block.getBlockFromItem(stack.getItem()) instanceof AmphitriteCofferBlock);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return this.world.getBlockState(this.pos).get(Properties.WATERLOGGED);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new AmphitriteCofferScreenHandler(syncId, playerInventory, this);
    }

    public boolean isConverting() {
        return shouldConvert;
    }

    public void setShouldConvert(boolean shouldConvert) {
        this.shouldConvert = shouldConvert;
    }

    public ItemEntity dropStack(ItemStack stack) {
        float mag = 0.5f + this.world.random.nextFloat() * 0.25f;
        float angle = this.world.random.nextFloat() * ((float)Math.PI * 2);
        float xoff = -MathHelper.sin(angle) * mag;
        float yoff = MathHelper.cos(angle) * mag;

        ItemEntity itemEntity = new ItemEntity(
                this.world,
                (float) this.pos.getX() + xoff * 2,
                (float) this.pos.getY() + yoff * 2,
                this.pos.getZ(),
                stack
        );
        itemEntity.setPickupDelay(40);
        itemEntity.setVelocity(xoff, 0.2f, yoff);

        return itemEntity;
    }

    /*
    @Override
    public void setStack(int slot, ItemStack stack) {
        Optional<io.github.orlouge.amphitritecoffer.AmphitriteCofferRecipe> recipe =
                AmphitriteCofferSlot.getConversionRecipe(this.world, stack);
        super.setStack(slot, AmphitriteCofferSlot.applyConversionRecipe(stack, recipe));
    }
    */
}
