package xyz.brassgoggledcoders.netherbarrel.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepContainerOpenersCounter;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepItemHandler;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelBlocks;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelContainers;
import xyz.brassgoggledcoders.netherbarrel.menu.NetherBarrelMenu;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public class NetherBarrelBlockEntity extends BlockEntity implements MenuProvider {

    private final DeepItemHandler itemHandler;
    private final LazyOptional<IItemHandler> lazyItemHandler;
    private final DeepContainerOpenersCounter containerOpenersCounter;

    private Component customName;

    public NetherBarrelBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.itemHandler = new DeepItemHandler(this::setChanged);
        this.lazyItemHandler = LazyOptional.of(this::getItemHandler);
        this.containerOpenersCounter = new DeepContainerOpenersCounter(itemHandler);
    }

    public NetherBarrelBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        this(NetherBarrelBlocks.NETHER_BARREL_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @NotNull
    public DeepItemHandler getItemHandler() {
        return this.itemHandler;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    public void startOpen(Player pPlayer) {
        if (!this.isRemoved() && !pPlayer.isSpectator() && this.getLevel() != null) {
            this.containerOpenersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player pPlayer) {
        if (!this.isRemoved() && !pPlayer.isSpectator() && this.getLevel() != null) {
            this.containerOpenersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.isRemoved() && this.getLevel() != null) {
            this.containerOpenersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void setCustomName(Component textComponent) {
        this.customName = textComponent;
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return Objects.requireNonNullElseGet(this.customName, () -> this.getBlockState()
                .getBlock()
                .getName());
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new NetherBarrelMenu(
                NetherBarrelContainers.NETHER_BARREL_CONTAINER.get(),
                pContainerId,
                pInventory,
                this::stillValid,
                (player, opening) -> {
                    if (opening) {
                        this.startOpen(player);
                    } else {
                        this.stopOpen(player);
                    }
                },
                this.itemHandler
        );
    }

    public boolean stillValid(Player pPlayer) {
        if (this.getLevel() == null || this.getLevel().getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(pPlayer.distanceToSqr((double) this.worldPosition.getX() + 0.5D,
                    (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D
            ) > 64.0D);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.itemHandler.deserializeNBT(pTag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("Inventory", this.itemHandler.serializeNBT());
    }
}
