package xyz.brassgoggledcoders.netherbarrel.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.netherbarrel.capability.DeepItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class NetherBarrelMenu extends AbstractContainerMenu {
    private static final int SLOTS_PER_ROW = 9;

    private final Predicate<Player> stillValid;
    private final BiConsumer<Player, Boolean> openHandler;

    private final DeepItemHandler itemHandler;
    private final int containerRows;
    private final Inventory inventory;

    public NetherBarrelMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory,
                            Predicate<Player> stillValid, BiConsumer<Player, Boolean> openHandler,
                            DeepItemHandler handler) {
        super(pMenuType, pContainerId);
        this.stillValid = stillValid;
        this.openHandler = openHandler;
        this.openHandler.accept(inventory.player, true);
        this.inventory = inventory;
        this.itemHandler = handler;
        this.containerRows = 3;
        int i = (containerRows - 4) * 18;

        for (int row = 0; row < containerRows; ++row) {
            for (int column = 0; column < SLOTS_PER_ROW; ++column) {
                this.addSlot(new DeepSlot(handler, column + row * SLOTS_PER_ROW, 8 + column * 18, 18 + row * 18));
            }
        }

        for (int playerRow = 0; playerRow < 3; ++playerRow) {
            for (int playerColumn = 0; playerColumn < SLOTS_PER_ROW; ++playerColumn) {
                this.addSlot(new Slot(inventory, playerColumn + playerRow * 9 + 9, 8 + playerColumn * 18, 103 + playerRow * 18 + i));
            }
        }

        for (int hotBarSlot = 0; hotBarSlot < SLOTS_PER_ROW; ++hotBarSlot) {
            this.addSlot(new Slot(inventory, hotBarSlot, 8 + hotBarSlot * 18, 161 + i));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid.test(pPlayer);
    }

    @Override
    protected boolean moveItemStackTo(@NotNull ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (pReverseDirection) {
            i = pEndIndex - 1;
        }

        while (!pStack.isEmpty()) {
            if (pReverseDirection) {
                if (i < pStartIndex) {
                    break;
                }
            } else if (i >= pEndIndex) {
                break;
            }

            Slot slot = this.slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(pStack, itemstack)) {
                int j = itemstack.getCount() + pStack.getCount();
                int maxSize = Math.min(slot.getMaxStackSize(), pStack.getMaxStackSize());
                if (slot instanceof DeepSlot) {
                    maxSize = slot.getMaxStackSize(pStack);
                }
                if (j <= maxSize) {
                    pStack.setCount(0);
                    itemstack.setCount(j);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    pStack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.setChanged();
                    flag = true;
                }
            }

            if (pReverseDirection) {
                --i;
            } else {
                ++i;
            }
        }

        if (!pStack.isEmpty()) {
            if (pReverseDirection) {
                i = pEndIndex - 1;
            } else {
                i = pStartIndex;
            }

            while (true) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemStack1 = slot1.getItem();
                if (itemStack1.isEmpty() && slot1.mayPlace(pStack)) {
                    if (pStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(pStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(pStack.split(pStack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public void setSynchronizer(@NotNull ContainerSynchronizer containerSynchronizer) {
        if (inventory.player instanceof ServerPlayer serverPlayer) {
            super.setSynchronizer(new NetherBarrelContainerSynchronizer(serverPlayer, containerSynchronizer));
        } else {
            super.setSynchronizer(containerSynchronizer);
        }
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        int amountMoved = 0;
        if (slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            itemstack = slotItemStack.copy();
            if (pIndex < this.containerRows * SLOTS_PER_ROW) {
                ItemStack testStack = slotItemStack.copy();
                testStack.setCount(Math.min(slotItemStack.getMaxStackSize(), testStack.getCount()));
                boolean flag = !this.moveItemStackTo(testStack, this.containerRows * SLOTS_PER_ROW, this.slots.size(), true);
                if (testStack.getCount() != slotItemStack.getMaxStackSize()) {
                    slotItemStack.shrink(slotItemStack.getMaxStackSize() - testStack.getCount());
                    amountMoved = slotItemStack.getMaxStackSize() - testStack.getCount();
                }
                if (flag) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItemStack, 0, this.containerRows * SLOTS_PER_ROW, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItemStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        if (amountMoved > 0) {
            return ItemStack.EMPTY;
        } else {
            return itemstack;
        }
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.openHandler.accept(pPlayer, false);
    }

    public DeepItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public int getRowCount() {
        return containerRows;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void clicked(int slotNumber, int button, ClickType clickType, Player player) {
        boolean didSomething = false;

        if (slotNumber >= 0 && slotNumber < this.itemHandler.getSlots()) {
            ClickAction clickAction = null;
            if (button == 0) {
                clickAction = ClickAction.PRIMARY;
            } else if (button == 1) {
                clickAction = ClickAction.SECONDARY;
            }

            Slot slot = this.slots.get(slotNumber);
            if (clickType == ClickType.PICKUP && clickAction == ClickAction.SECONDARY) {
                if (this.getCarried().isEmpty()) {
                    ItemStack slotStack = slot.getItem();
                    int count = slotStack.getCount();
                    if (count > slotStack.getMaxStackSize()) {
                        count = slotStack.getMaxStackSize();
                    }
                    int toPull = (count + 1) / 2;
                    didSomething = slot.tryRemove(toPull, Integer.MAX_VALUE, player)
                            .map((item) -> {
                                this.setCarried(item);
                                slot.onTake(player, item);
                                return true;
                            })
                            .orElse(false);
                }
            }
        }

        if (!didSomething) {
            super.clicked(slotNumber, button, clickType, player);
        }
    }

    @NotNull
    public static NetherBarrelMenu create(MenuType<NetherBarrelMenu> menuType, int containerId, Inventory inventory) {
        return new NetherBarrelMenu(
                menuType,
                containerId,
                inventory,
                player -> true,
                (player, opening) -> {
                },
                new DeepItemHandler(() -> {
                })
        );
    }
}
