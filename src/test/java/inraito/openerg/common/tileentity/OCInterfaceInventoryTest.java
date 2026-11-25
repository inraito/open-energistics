package inraito.openerg.common.tileentity;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import inraito.openerg.HeadlessItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OCInterfaceInventoryTest extends HeadlessItems {
    private final OCInterfaceTileEntity tile = mock(OCInterfaceTileEntity.class, CALLS_REAL_METHODS);
    private final ItemStackHandler storage = new ItemStackHandler(1);
    private final List<ItemStack> pending = new ArrayList<>();
    private final Map<ItemStack, OCInterfaceTileEntity.CraftingContext> patterns = new HashMap<>();
    private final Map<ICraftingPatternDetails, ItemStack> cache = new HashMap<>();

    @Before
    public void supplyInventory() throws Exception {
        set("storageInventory", storage);
        set("waitingToSend", pending);
        set("craftingPatterns", patterns);
        set("cachedDetails", cache);
        tile.configInventory = new ItemStackHandler(1);
        doNothing().when(tile).setChanged();
    }

    private void set(String name, Object value) throws Exception {
        Field field = OCInterfaceTileEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(tile, value);
    }

    @Test
    public void repeatedLoadRestoresExactlyOnePendingBatch() {
        CompoundNBT saved = new CompoundNBT();
        ListNBT waiting = new ListNBT();
        waiting.add(new ItemStack(Items.COAL).save(new CompoundNBT()));
        saved.put("waiting_stacks", waiting);
        saved.put("pending_context", new OCInterfaceTileEntity.CraftingContext("chip").serializeNBT());
        saved.put("crafting_patterns", new ListNBT());
        tile.load(null, saved);
        assertTrue(tile.isBusy());
        tile.load(null, saved);
        assertEquals(1, pending.size());
    }

    @Test
    public void acceptedOverflowIsMarkedDirtyAndBlocksFurtherRequests() {
        ItemStack encoded = new ItemStack(Items.PAPER);
        ICraftingPatternDetails known = mock(ICraftingPatternDetails.class);
        cache.put(known, encoded);
        patterns.put(encoded, new OCInterfaceTileEntity.CraftingContext("chip"));
        storage.setStackInSlot(0, new ItemStack(Items.COAL, 63));
        CraftingInventory input = mock(CraftingInventory.class);
        when(input.getContainerSize()).thenReturn(1);
        when(input.getItem(0)).thenReturn(new ItemStack(Items.COAL, 4));
        assertTrue(tile.pushPattern(known, input));
        assertEquals(64, storage.getStackInSlot(0).getCount());
        assertEquals(3, pending.get(0).getCount());
        assertTrue(tile.isBusy());
        assertFalse(tile.pushPattern(known, input));
        verify(tile).setChanged();
    }

    @Test
    public void unknownPatternIsRejectedBeforeAccessingInventory() {
        CraftingInventory table = mock(CraftingInventory.class);
        assertFalse(tile.pushPattern(mock(ICraftingPatternDetails.class), table));
        verifyNoInteractions(table);
    }
}
