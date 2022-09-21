package xyz.brassgoggledcoders.netherbarrel.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class NetherBarrelSetSlotPacket {
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public NetherBarrelSetSlotPacket(int pContainerId, int pStateId, int pSlot, ItemStack pItemStack) {
        this.containerId = pContainerId;
        this.stateId = pStateId;
        this.slot = pSlot;
        this.itemStack = pItemStack.copy();
    }

    public NetherBarrelSetSlotPacket(FriendlyByteBuf pBuffer) {
        this.containerId = pBuffer.readByte();
        this.stateId = pBuffer.readVarInt();
        this.slot = pBuffer.readShort();
        this.itemStack = NetherBarrelSetContentsPacket.readItemStack(pBuffer);
    }

    /**
     * Writes the raw packet data to the data stream.
     */


    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }

    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeByte(this.containerId);
        pBuffer.writeVarInt(this.stateId);
        pBuffer.writeShort(this.slot);
        NetherBarrelSetContentsPacket.writeItemStack(pBuffer, this.itemStack);
    }

    public boolean consume(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            LocalPlayer playerEntity = Minecraft.getInstance().player;
            if (playerEntity != null && playerEntity.containerMenu.containerId == this.getContainerId()) {
                playerEntity.containerMenu.setItem(this.getSlot(), this.getStateId(), this.getItem());
            }
        });
        return true;
    }
}
