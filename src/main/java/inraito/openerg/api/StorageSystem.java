package inraito.openerg.api;

import inraito.openerg.common.tileentity.storage.StorageSystemReceiver;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Used with {@link StorageSystemReceiver}, these two
 * classes will provide a convenient way to implement the storage system standard on oc component.
 */
public interface StorageSystem {
    /**
     * Get how many slots this storage system have.
     * @return number of slots
     */
    int getSlotNum();

    /**
     * Push the given ItemStack into the given slot.
     * @param slot slot to push to
     * @param itemStack ItemStack to push
     * @return remainder
     */
    ItemStack push(int slot, ItemStack itemStack);

    /**
     * Pop the ItemStack in the given slot
     * @param slot slot to pop from
     * @param num number to pop
     * @return popped ItemStack
     */
    ItemStack pop(int slot, int num);

    /**
     * Get ItemStack in the given slot. <b>Don't modify the ItemStack returned</b>.
     * @param slot slot index
     * @return read-only ItemStack in the given slot
     */
    @Nullable
    ItemStack check(int slot);
}
