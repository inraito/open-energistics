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
     * Get how many slots this storage system have. Not always valid since some storage system
     * may not be treated as a large, consistent inventory.
     * @return number of slots or -1 if not applicable
     */
    int getSlotNum();

    /**
     * Push the given ItemStack into the given slot.
     * @param slot slot to push to
     * @param itemStack ItemStack to push
     * @return remainder
     */
    default ItemStack push(int slot, ItemStack itemStack){
        return this.push(slot, itemStack, false);
    }

    /**
     * Same as {@link StorageSystem#push(int, ItemStack)}, but if atomic is true,
     * the given itemStack will be pushed all or none, that is the returned stack
     * will either be empty or be the very one that are passed in.
     */
    ItemStack push(int slot, ItemStack itemStack, boolean atomic);

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
