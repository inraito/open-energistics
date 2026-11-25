package inraito.openerg.util;

import inraito.openerg.HeadlessItems;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class ItemHandlerHelperTest extends HeadlessItems {
    @Test
    public void fullAndPartialSlotsReturnOnlyOverflow() {
        Inventory inputs = new Inventory(new ItemStack(Items.IRON_INGOT, 5), new ItemStack(Items.COAL, 2));
        ItemStackHandler output = new ItemStackHandler(2);
        output.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 62));
        output.setStackInSlot(1, new ItemStack(Items.COAL, 63));
        List<ItemStack> remaining = ItemHandlerHelper.pushAll(inputs, output);
        assertEquals(64, output.getStackInSlot(0).getCount());
        assertEquals(64, output.getStackInSlot(1).getCount());
        assertEquals(2, remaining.size());
        assertEquals(Items.IRON_INGOT, remaining.get(0).getItem());
        assertEquals(3, remaining.get(0).getCount());
        assertEquals(Items.COAL, remaining.get(1).getItem());
        assertEquals(1, remaining.get(1).getCount());
        assertEquals(5, inputs.getItem(0).getCount());
    }
}
