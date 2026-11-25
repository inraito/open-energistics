package inraito.openerg.common.tileentity;

import inraito.openerg.HeadlessItems;
import net.minecraft.item.Items;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.ItemStackHandler;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.API;
import li.cil.oc.api.detail.NetworkAPI;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.item.ItemStack;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OEInterfaceStorageTest extends HeadlessItems {
    private final OEInterfaceTileEntity tile = mock(OEInterfaceTileEntity.class, CALLS_REAL_METHODS);
    private final List<List<ItemStack>> jobs = new ArrayList<>();

    // Seed exactly the state after two accepted requests, bypassing the live AE and OC nodes.
    @Before
    public void seedIngredientWindow() throws Exception {
        setField("storage", jobs);
        setField("sequenceHead", 10);
        setField("sequenceTail", 13);
        setField("cachedDetails", new HashMap<>());
        setField("craftingPatterns", new HashMap<>());
        tile.configInventory = new ItemStackHandler(1);
        jobs.add(Arrays.asList(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.COAL, 2)));
        jobs.add(Arrays.asList(new ItemStack(Items.IRON_INGOT, 1)));
        doNothing().when(tile).setChanged();
    }

    @Test
    public void repeatedUpdateTagsReplacePatternsAndOldIds() throws Exception {
        CompoundNBT saved = new CompoundNBT();
        saved.put("storage", new ListNBT());
        CompoundNBT entry = new CompoundNBT();
        entry.put("stack", new ItemStack(Items.PAPER).save(new CompoundNBT()));
        entry.putString("id", "processor");
        ListNBT patterns = new ListNBT();
        patterns.add(entry);
        saved.put("crafting_patterns", patterns);
        tile.handleUpdateTag(null, saved);
        tile.handleUpdateTag(null, saved);
        int repeatedCount = ((Collection<?>) tile.listAllPatterns(null, null)[0]).size();
        saved.put("crafting_patterns", new ListNBT());
        tile.handleUpdateTag(null, saved);
        int clearedCount = ((Collection<?>) tile.listAllPatterns(null, null)[0]).size();
        assertArrayEquals(new int[]{1, 0}, new int[]{repeatedCount, clearedCount});
    }

    private Node connectForCrafting() throws Exception {
        Node node = mock(Node.class, RETURNS_DEEP_STUBS);
        Field field = TileEntityEnvironment.class.getDeclaredField("node");
        field.setAccessible(true);
        field.set(tile, node);
        jobs.clear();
        setField("sequenceHead", 0);
        setField("sequenceTail", 0);
        return node;
    }

    @Test
    public void broadcastsHalfOpenRangesForNonemptyIngredients() throws Exception {
        Node node = connectForCrafting();
        when(node.address()).thenReturn("interface");
        setField("port", 11037);
        ICraftingPatternDetails pattern = mock(ICraftingPatternDetails.class);
        ItemStack encoded = new ItemStack(Items.PAPER);
        setField("cachedDetails", java.util.Collections.singletonMap(pattern, encoded));
        setField("craftingPatterns", java.util.Collections.singletonMap(encoded, "processor"));
        CraftingInventory input = mock(CraftingInventory.class);
        when(input.getContainerSize()).thenReturn(3);
        when(input.getItem(0)).thenReturn(new ItemStack(Items.COAL));
        when(input.getItem(1)).thenReturn(ItemStack.EMPTY);
        when(input.getItem(2)).thenReturn(new ItemStack(Items.IRON_INGOT));
        NetworkAPI original = API.network;
        try {
            API.network = mock(NetworkAPI.class);
            Packet packet = mock(Packet.class);
            when(API.network.newPacket(eq("interface"), isNull(), eq(11037), any(Object[].class))).thenReturn(packet);
            assertTrue(tile.pushPattern(pattern, input));
            assertTrue(tile.pushPattern(pattern, input));
            verify(API.network).newPacket("interface", null, 11037, new Object[]{"oe_interface:processor", 0, 2});
            verify(API.network).newPacket("interface", null, 11037, new Object[]{"oe_interface:processor", 2, 4});
            verify(node.network(), times(2)).sendToReachable(node, "network.message", packet);
            assertEquals(Items.IRON_INGOT, tile.check(3).getItem());
        } finally {
            API.network = original;
        }
    }

    @Test
    public void unknownPatternDoesNotConsumeJobSlot() throws Exception {
        connectForCrafting();
        CraftingInventory table = mock(CraftingInventory.class);
        when(table.getContainerSize()).thenReturn(1);
        when(table.getItem(0)).thenReturn(new ItemStack(Items.COAL));
        assertFalse(tile.pushPattern(mock(ICraftingPatternDetails.class), table));
        assertTrue(jobs.isEmpty());
    }

    @Test
    public void emptyIngredientRequestsCannotPermanentlyFillWindow() throws Exception {
        connectForCrafting();
        ICraftingPatternDetails pattern = mock(ICraftingPatternDetails.class);
        ItemStack encoded = new ItemStack(Items.PAPER);
        setField("cachedDetails", java.util.Collections.singletonMap(pattern, encoded));
        setField("craftingPatterns", java.util.Collections.singletonMap(encoded, "sample"));
        CraftingInventory empty = mock(CraftingInventory.class);
        for (int i = 0; i < OEInterfaceTileEntity.MAX_STORAGE_SIZE; i++) tile.pushPattern(pattern, empty);
        assertArrayEquals(new Object[]{0, 0}, tile.sequence(null, null));
        assertFalse("no ingredient slots exist to drain", tile.isBusy());
    }

    private void setField(String name, Object value) throws Exception {
        Field field = OEInterfaceTileEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(tile, value);
    }

    @Test
    public void releasesOnlyContiguousFinishedJobs() throws Exception {
        assertEquals(1, tile.pop(12, 1).getCount());
        assertArrayEquals(new Object[]{10, 13}, tile.sequence(null, null));
        assertEquals(2, tile.pop(10, 2).getCount());
        assertEquals(1, tile.check(10).getCount());
        assertFalse((boolean) tile.resetSequence(null, null)[0]);
        tile.pop(10, 1);
        tile.pop(11, 2);
        assertArrayEquals(new Object[]{13, 13}, tile.sequence(null, null));
        assertTrue(tile.check(10).isEmpty());
        assertTrue((boolean) tile.resetSequence(null, null)[0]);
        assertArrayEquals(new Object[]{0, 0}, tile.sequence(null, null));
    }

    @Test
    public void extractingIngredientsMarksSavedStateDirty() {
        assertEquals(1, tile.pop(10, 1).getCount());
        verify(tile).setChanged();
    }

    @Test
    public void minusOneExtractsWholeStackAsStorageProtocolSpecifies() {
        ItemStack extracted = tile.pop(10, -1);
        assertEquals("quantity left in source: " + tile.check(10).getCount(), 3, extracted.getCount());
        assertTrue(tile.check(10).isEmpty());
    }

    @Test
    public void invalidSlotAndRejectedInsertLeaveWindowIntact() {
        assertTrue(tile.pop(9, 1).isEmpty());
        assertTrue(tile.check(13).isEmpty());
        ItemStack ingredient = tile.check(11);
        assertSame(ingredient, tile.push(10, ingredient, true));
        assertEquals(3, tile.check(10).getCount());
    }
}
