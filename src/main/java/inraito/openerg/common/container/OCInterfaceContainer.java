package inraito.openerg.common.container;

import appeng.core.Api;
import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class OCInterfaceContainer extends Container {
    private final int shift = 27;
    /**
     * used only on client, presumably.
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
     * used on server.
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
