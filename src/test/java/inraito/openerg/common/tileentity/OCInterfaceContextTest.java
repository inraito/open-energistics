package inraito.openerg.common.tileentity;

import inraito.openerg.common.tileentity.OCInterfaceTileEntity.CraftingContext;
import net.minecraft.nbt.CompoundNBT;
import org.junit.Test;

import static org.junit.Assert.*;

public class OCInterfaceContextTest {
    @Test
    public void equalEmptyContextsHaveEqualHashes() {
        CraftingContext empty = new CraftingContext();
        CraftingContext reused = new CraftingContext("previous");
        reused.deserializeNBT(empty.serializeNBT());
        assertEquals(empty, reused);
        assertEquals(empty.hashCode(), reused.hashCode());
    }

    @Test
    public void pendingMessageSurvivesNbtRoundTrip() {
        CraftingContext pending = new CraftingContext("processor");
        CompoundNBT saved = pending.serializeNBT();
        assertEquals("processor", saved.getString("message"));

        CraftingContext restored = new CraftingContext();
        restored.deserializeNBT(saved);
        assertFalse(restored.isEmpty());
        assertEquals(pending, restored);
    }

    @Test
    public void emptySaveClearsPendingContext() {
        CraftingContext restored = new CraftingContext("processor");
        restored.deserializeNBT(new CraftingContext().serializeNBT());
        assertTrue(restored.isEmpty());
        assertFalse(restored.serializeNBT().contains("message"));
    }
}
