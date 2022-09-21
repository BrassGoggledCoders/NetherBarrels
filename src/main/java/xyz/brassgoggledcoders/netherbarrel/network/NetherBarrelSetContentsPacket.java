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

public class NetherBarrelSetContentsPacket {
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public NetherBarrelSetContentsPacket(int pContainerId, int pStateId, NonNullList<ItemStack> pItems, ItemStack pCarriedItem) {
        this.containerId = pContainerId;
        this.stateId = pStateId;
        this.items = NonNullList.withSize(pItems.size(), ItemStack.EMPTY);

        for (int i = 0; i < pItems.size(); ++i) {
            this.items.set(i, pItems.get(i).copy());
        }

        this.carriedItem = pCarriedItem.copy();
    }

    public NetherBarrelSetContentsPacket(FriendlyByteBuf pBuffer) {
        this.containerId = pBuffer.readUnsignedByte();
        this.stateId = pBuffer.readVarInt();
        this.items = pBuffer.readCollection(NonNullList::createWithCapacity, NetherBarrelSetContentsPacket::readItemStack);
        this.carriedItem = pBuffer.readItem();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeByte(this.containerId);
        pBuffer.writeVarInt(this.stateId);
        pBuffer.writeCollection(this.items, NetherBarrelSetContentsPacket::writeItemStack);
        pBuffer.writeItem(this.carriedItem);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public ItemStack getCarriedItem() {
        return this.carriedItem;
    }

    public int getStateId() {
        return this.stateId;
    }

    public boolean consumer(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            LocalPlayer playerEntity = Minecraft.getInstance().player;
            if (playerEntity != null && playerEntity.containerMenu.containerId == this.getContainerId()) {
                playerEntity.containerMenu.initializeContents(this.getStateId(), this.getItems(), this.getCarriedItem());
            }
        });
        return true;
    }

    public static void writeItemStack(FriendlyByteBuf friendlyByteBuf, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            friendlyByteBuf.writeBoolean(false);
        } else {
            friendlyByteBuf.writeBoolean(true);
            Item item = itemStack.getItem();
            friendlyByteBuf.writeVarInt(Item.getId(item));
            friendlyByteBuf.writeVarInt(itemStack.getCount());
            CompoundTag compoundTag = null;
            if (item.isDamageable(itemStack) || item.shouldOverrideMultiplayerNbt()) {
                compoundTag = item.getShareTag(itemStack);
            }

            friendlyByteBuf.writeNbt(compoundTag);
        }
    }

    public static ItemStack readItemStack(FriendlyByteBuf friendlyByteBuf) {
        if (!friendlyByteBuf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = friendlyByteBuf.readVarInt();
            int j = friendlyByteBuf.readVarInt();
            ItemStack itemStack = new ItemStack(Item.byId(i), j);
            itemStack.readShareTag(friendlyByteBuf.readNbt());
            return itemStack;
        }
    }
}
