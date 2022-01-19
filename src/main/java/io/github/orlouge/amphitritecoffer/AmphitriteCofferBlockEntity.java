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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
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
    private DefaultedList<ItemStack> chargeItemStacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private boolean shouldConvert = true;
    private int charge = 0;
    private boolean updateCharged = false;

    public AmphitriteCofferBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK_ENTITY, blockPos, blockState);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AmphitriteCofferBlockEntity blockEntity) {
        ItemStack chargeStack = blockEntity.chargeItemStacks.get(0);
        if (blockEntity.charge <= 0 && chargeStack.isOf(Items.HEART_OF_THE_SEA)) {
            blockEntity.charge = AmphitriteCofferMod.CONFIG.chargePerHeart;
            blockEntity.updateCharged = true;
            chargeStack.decrement(1);
        }

        if (blockEntity.updateCharged) {
            updateChargedState(world, pos, state, blockEntity);
            AmphitriteCofferBlockEntity.markDirty(world, pos, state);
            blockEntity.updateCharged = false;
        }
    }

    public static void updateChargedState(World world, BlockPos pos, BlockState state, AmphitriteCofferBlockEntity blockEntity) {
        if (!(state.getBlock() instanceof AmphitriteCofferBlock)) {
            return;
        }
        state = (BlockState) state.with(AmphitriteCofferBlock.CHARGED, blockEntity.charge > 0);
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
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
        return Text.of("");
    }



    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt) && nbt.contains("Items", 9)) {
            Inventories.readNbt(nbt, this.inventory);
        }
        this.charge = nbt.getInt("Charge");
        this.updateCharged = true;
        if (nbt.contains("ChargeInventory")) {
            NbtCompound nbtChargeInventory = nbt.getCompound("ChargeInventory");
            if (nbtChargeInventory.contains("Items", 9)) {
                Inventories.readNbt(nbtChargeInventory, this.chargeItemStacks);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, false);
        }
        nbt.putInt("Charge", charge);
        NbtCompound nbtChargeInventory = new NbtCompound();
        Inventories.writeNbt(nbtChargeInventory, this.chargeItemStacks, false);
        nbt.put("ChargeInventory", nbtChargeInventory);
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
        return canOpen() &&
               stack.getCount() <= 1 &&
               !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) &&
               !(Block.getBlockFromItem(stack.getItem()) instanceof AmphitriteCofferBlock);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return canOpen();
    }

    private boolean canOpen() {
        return this.world.getBlockState(this.pos).get(AmphitriteCofferBlock.CHARGED) ||
               this.world.getBlockState(this.pos).get(Properties.WATERLOGGED);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new AmphitriteCofferScreenHandler(syncId, playerInventory, this, this.chargeInventory, this.propertyDelegate);
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

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return charge;
        }

        @Override
        public void set(int index, int value) {
            if (value >= 0) {
                charge = value;
                updateCharged = true;
            } else {
                ItemStack chargeStack = chargeItemStacks.get(0);
                value = -value;
                int required = value / AmphitriteCofferMod.CONFIG.chargePerHeart +
                               value % AmphitriteCofferMod.CONFIG.chargePerHeart == 0 ? 0 : 1;
                if (chargeStack.isOf(Items.HEART_OF_THE_SEA) && chargeStack.getCount() >= required) {
                    charge += required * AmphitriteCofferMod.CONFIG.chargePerHeart;
                    updateCharged = true;
                    chargeStack.decrement(required);
                }
            }
        }

        @Override
        public int size() {
            return 1;
        }
    };

    // public DefaultedList<ItemStack> droppedInventory() {
    //     return this.chargeItemStacks;
    // }

    private Inventory chargeInventory = new Inventory() {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return chargeItemStacks.get(0).isEmpty();
        }

        @Override
        public ItemStack getStack(int slot) {
            return chargeItemStacks.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack result = Inventories.splitStack(chargeItemStacks, slot, amount);
            if (!result.isEmpty()) {
                markDirty();
            }
            return result;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(chargeItemStacks, slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            chargeItemStacks.set(slot, stack);
            if (stack.getCount() > getMaxCountPerStack()) {
                stack.setCount(getMaxCountPerStack());
            }
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            chargeItemStacks.clear();
        }
    };

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
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
