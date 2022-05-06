package xyz.brassgoggledcoders.netherbarrel.capability;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class DeepItemHandler implements IItemHandlerModifiable {
    private final NonNullList<ItemStack> stacks;
    private final int slots;

    public DeepItemHandler() {
        this(27);
    }

    public DeepItemHandler(int slots) {
        this.slots = slots;
        this.stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.stacks.set(slot, stack);
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
        return null;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * 8;
    }

    private int getItemStackLimit(int slot) {
        ItemStack itemStack = this.stacks.get(slot);
        return itemStack.isDamageableItem() ? itemStack.getMaxStackSize() : itemStack.getMaxStackSize() * 8;
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
                proportion += (float) itemstack.getCount() / (float) Math.min(this.getSlotLimit(j), this.getItemStackLimit(j));
                ++itemsFound;
            }
        }

        proportion = proportion / (float) this.getSlots();
        return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
    }
}
