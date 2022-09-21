package xyz.brassgoggledcoders.netherbarrel.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.brassgoggledcoders.netherbarrel.NetherBarrels;

public class NetworkHandler {
    private static final String VERSION = "1";

    private static NetworkHandler instance;

    private final SimpleChannel simpleChannel;

    public NetworkHandler() {
        this.simpleChannel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(NetherBarrels.ID, "network"),
                () -> VERSION,
                VERSION::matches,
                VERSION::matches
        );

        this.simpleChannel.messageBuilder(NetherBarrelSetContentsPacket.class, 0)
                .encoder(NetherBarrelSetContentsPacket::write)
                .decoder(NetherBarrelSetContentsPacket::new)
                .consumer(NetherBarrelSetContentsPacket::consumer)
                .add();

        this.simpleChannel.messageBuilder(NetherBarrelSetSlotPacket.class, 1)
                .encoder(NetherBarrelSetSlotPacket::write)
                .decoder(NetherBarrelSetSlotPacket::new)
                .consumer(NetherBarrelSetSlotPacket::consume)
                .add();
    }

    public void sendSetContentsPacket(ServerPlayer serverPlayer, NetherBarrelSetContentsPacket packet) {
        this.simpleChannel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
    }

    public void sendSetSlotPacket(ServerPlayer serverPlayer, NetherBarrelSetSlotPacket packet) {
        this.simpleChannel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
    }

    public static NetworkHandler getInstance() {
        if (instance == null) {
            instance = new NetworkHandler();
        }
        return instance;
    }
}
