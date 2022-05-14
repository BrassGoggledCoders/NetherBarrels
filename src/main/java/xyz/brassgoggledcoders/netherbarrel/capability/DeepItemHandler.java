package xyz.brassgoggledcoders.netherbarrel.capability;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class DeepItemHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {
    private static final int MULTIPLIER = 8;

    private final NonNullList<ItemStack> stacks;
    private final int slots;
    private final Runnable onContentsChanged;

    public DeepItemHandler(Runnable onContentsChanged) {
        this(27, onContentsChanged);
    }

    public DeepItemHandler(int slots, Runnable onContentsChanged) {
        this.slots = slots;
        this.stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
        this.onContentsChanged = onContentsChanged;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.stacks.set(slot, stack);
        this.onContentsChanged.run();
    }

    @Override
    public int getSlots() {
        return this.slots;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!stack.isEmpty()) {

            if (isItemValid(slot, stack)) {
                validateSlotIndex(slot);

                ItemStack existing = this.stacks.get(slot);

                int limit = getItemStackLimit(slot, stack);

                if (!existing.isEmpty()) {
                    if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                        return stack;
                    }

                    limit -= existing.getCount();
                }

                if (limit <= 0) {
                    return stack;
                }

                boolean reachedLimit = stack.getCount() > limit;

                if (!simulate) {
                    if (existing.isEmpty()) {
                        this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                    } else {
                        existing.grow(reachedLimit ? limit : stack.getCount());
                    }
                    onContentsChanged.run();
                }

                return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
            } else {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount > 0) {
            validateSlotIndex(slot);

            ItemStack existing = this.stacks.get(slot);

            if (!existing.isEmpty()) {
                int toExtract = Math.min(amount, existing.getMaxStackSize());

                if (existing.getCount() <= toExtract) {
                    if (!simulate) {
                        this.stacks.set(slot, ItemStack.EMPTY);
                        onContentsChanged.run();
                        return existing;
                    } else {
                        return existing.copy();
                    }
                } else {
                    if (!simulate) {
                        this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                        onContentsChanged.run();
                    }

                    return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * MULTIPLIER;
    }

    public int getItemStackLimit(int slot, ItemStack itemStack) {
        return Math.min(
                itemStack.isDamageableItem() && itemStack.isDamaged() ? itemStack.getMaxStackSize() : itemStack.getMaxStackSize() * MULTIPLIER,
                this.getSlotLimit(slot)
        );
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    public int calculateComparator() {
        int itemsFound = 0;
        float proportion = 0.0F;

        for (int j = 0; j < this.getSlots(); ++j) {
            ItemStack itemstack = this.getStackInSlot(j);

            if (!itemstack.isEmpty()) {
                proportion += (float) itemstack.getCount() / (float) this.getItemStackLimit(j, itemstack);
                ++itemsFound;
            }
        }

        proportion = proportion / (float) this.getSlots();
        return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack itemStack = stacks.get(i);
            if (!itemStack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemStack.save(itemTag);
                itemTag.putInt("Count", itemStack.getCount());
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack itemStack = ItemStack.of(itemTags);
                itemStack.setCount(itemTags.getInt("Count"));
                stacks.set(slot, itemStack);
            }
        }
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }
}
