package inraito.openerg.common.container;

import appeng.core.Api;
import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.SlotItemHandler;

public class OCInterfaceContainer extends Container {
    private final int shift = 27;
    /**
     * Used only on client, in other words, used by {@link net.minecraftforge.common.extensions.IForgeContainerType#create(IContainerFactory)}.
     * And the {@link #addSlot(Slot)} and {@link #addDataSlot(IntReferenceHolder)} methods will add them to the slot list, which will be used
     * to properly render the highlight(when you move your cursor on top of it) and the ItemStack's dragging and placing. Also, the list is
     * used when sync packets from server are received. As far as i understand it, the only identifier of a slot in this syncing process, is
     * the index of it, i.e. the position of their adding actions in the timeline, though i haven't verify that.<br/><br/>
     *
     * Slots can be heterogeneous, both on the same side and on different side, just as i wrote.
     * Because the container's data should be synced from its counterpart on the server side, it's weird to pass the tileentity
     * it attached to and the data inside to it. Therefore we simply create new {@link IInventory} and use that to add slots.
     */
    protected OCInterfaceContainer(int pContainerId) {
        super(ContainerList.ocInterfaceContainer.get(), pContainerId);
        Inventory inventory = new Inventory(64);
        //only encoded patterns are accepted
        Slot configSlot = new Slot(inventory,0,79,31- shift){
            @Override
            public boolean mayPlace(ItemStack pStack) {
                return Api.instance().crafting().isEncodedPattern(pStack);
            }
        };
        this.addSlot(configSlot);
        inventory = new Inventory(64);
        for(int i=0;i<9;i++){
            this.addSlot(new Slot(inventory,i,8+i*18,71 - shift));
        }
        for(int i=9;i<18;i++){
            this.addSlot(new Slot(inventory,i,8+(i-9)*18,89 - shift));
        }
        for(int i = 18;i<27;i++){
            this.addSlot(new Slot(inventory,i,8+(i-18)*18,107 - shift));
        }
        layoutPlayerInventorySlots(new Inventory(64), 8, 139 - shift);
    }

    /**
     * Used on server, in other words, used by {@link net.minecraft.inventory.container.INamedContainerProvider#createMenu(int, PlayerInventory, PlayerEntity)}.
     * And the {@link #addSlot(Slot)} and {@link #addDataSlot(IntReferenceHolder)} methods basically add them to a tracking list, which will be
     * iterated, serialized and then sent to client, during the, shall we say, server-to-client sync phase. Besides, there's a client-to-server sync,
     * in which client sends packets about how ItemStacks are moved by player. So in this process, the tracking list also contributes to properly handle
     * that information from client, making them actually take effect.
     * @param tileEntity tileentity of the OCInterface.
     */
    public OCInterfaceContainer(int pContainerId, OCInterfaceTileEntity tileEntity,PlayerEntity player) {
        super(ContainerList.ocInterfaceContainer.get(), pContainerId);
        this.addSlot(new SlotItemHandler(tileEntity.configInventory,0,79,31 -shift));
        for(int i=0;i<9;i++){
            this.addSlot(new SlotItemHandler(tileEntity.storageInventory,i,8+i*18,71 - shift));
        }
        for(int i=9;i<18;i++){
            this.addSlot(new SlotItemHandler(tileEntity.storageInventory,i,8+(i-9)*18,89 - shift));
        }
        for(int i = 18;i<27;i++){
            this.addSlot(new SlotItemHandler(tileEntity.storageInventory,i,8+(i-18)*18,107 -shift));
        }
        layoutPlayerInventorySlots(player.inventory, 8, 139-shift);
    }
    
    private void layoutPlayerInventorySlots(IInventory inventory, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(inventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(inventory, 0, leftCol, topRow, 9, 18);
    }
    
    private int addSlotRange(IInventory inventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(inventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }
    
    private int addSlotBox(IInventory inventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(inventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }
    
    @Override
    public boolean stillValid(PlayerEntity pPlayer) {
        return true;
    }
    
    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 28) {
                if (!this.moveItemStackTo(itemstack1, 28, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 28, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
