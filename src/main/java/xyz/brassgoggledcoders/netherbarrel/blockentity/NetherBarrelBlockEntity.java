package xyz.brassgoggledcoders.netherbarrel.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepItemHandler;

public class NetherBarrelBlockEntity extends BlockEntity {

    private final DeepItemHandler itemHandler;
    private final LazyOptional<IItemHandler> lazyItemHandler;

    public NetherBarrelBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.itemHandler = new DeepItemHandler();
        this.lazyItemHandler = LazyOptional.of(this::getItemHandler);
    }

    @NotNull
    public DeepItemHandler getItemHandler() {
        return this.itemHandler;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }
}
