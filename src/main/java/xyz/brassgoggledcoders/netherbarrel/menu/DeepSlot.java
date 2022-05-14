package xyz.brassgoggledcoders.netherbarrel.menu;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepItemHandler;

public class DeepSlot extends SlotItemHandler {
    private final DeepItemHandler deepItemHandler;

    public DeepSlot(DeepItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.deepItemHandler = itemHandler;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return deepItemHandler.getItemStackLimit(this.getContainerSlot(), stack);
    }
}
