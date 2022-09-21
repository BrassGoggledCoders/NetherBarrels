package xyz.brassgoggledcoders.netherbarrel.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.netherbarrel.network.NetherBarrelSetContentsPacket;
import xyz.brassgoggledcoders.netherbarrel.network.NetherBarrelSetSlotPacket;
import xyz.brassgoggledcoders.netherbarrel.network.NetworkHandler;

import javax.annotation.ParametersAreNonnullByDefault;

public class NetherBarrelContainerSynchronizer implements ContainerSynchronizer {
    private final ServerPlayer serverPlayer;
    private final ContainerSynchronizer containerSynchronizer;

    public NetherBarrelContainerSynchronizer(ServerPlayer serverPlayer, ContainerSynchronizer containerSynchronizer) {
        this.serverPlayer = serverPlayer;
        this.containerSynchronizer = containerSynchronizer;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> itemStacks, ItemStack carried, int[] data) {
        NetworkHandler.getInstance().sendSetContentsPacket(
                serverPlayer,
                new NetherBarrelSetContentsPacket(
                        menu.containerId,
                        menu.incrementStateId(),
                        itemStacks,
                        carried
                )
        );

        for (int i = 0; i < data.length; ++i) {
            serverPlayer.connection.send(new ClientboundContainerSetDataPacket(menu.containerId, i, data[i]));
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack itemStack) {
        NetworkHandler.getInstance().sendSetSlotPacket(
                serverPlayer,
                new NetherBarrelSetSlotPacket(
                        menu.containerId,
                        menu.incrementStateId(),
                        slot,
                        itemStack
                )
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    public void sendCarriedChange(AbstractContainerMenu menu, ItemStack carried) {
        containerSynchronizer.sendCarriedChange(menu, carried);
    }

    @Override
    public void sendDataChange(@NotNull AbstractContainerMenu menu, int index, int value) {
        containerSynchronizer.sendDataChange(menu, index, value);
    }
}
