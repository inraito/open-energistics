package inraito.openerg.util;

import inraito.openerg.util.nbt.NBT2Collection;
import net.minecraft.nbt.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NBT2CollectionTest {
    @Test
    public void nestedCompoundsAndSupportedNumbersSurvive() {
        CompoundNBT root = new CompoundNBT();
        CompoundNBT nested = new CompoundNBT();
        nested.putString("name", "ore");
        nested.putFloat("efficiency", 1.5f);
        root.put("machine", nested);
        ListNBT ids = new ListNBT();
        ids.add(IntNBT.valueOf(2));
        ids.add(IntNBT.valueOf(7));
        root.put("ids", ids);
        Map<String, Object> converted = NBT2Collection.toMap(root);
        assertEquals(Arrays.asList(2, 7), converted.get("ids"));
        Map<?, ?> machine = (Map<?, ?>) converted.get("machine");
        assertEquals("ore", machine.get("name"));
        assertEquals(1.5, machine.get("efficiency"));
    }

    @Test
    public void descriptorPreservesStandardTagFields() {
        CompoundNBT root = new CompoundNBT();
        root.putBoolean("powered", true);
        root.putShort("tier", (short) 2);
        root.putLong("energy", 9000000000L);
        root.putDouble("temperature", 2.25);
        root.putIntArray("colors", new int[]{1, 2});
        assertEquals(root.getAllKeys(), NBT2Collection.toMap(root).keySet());
    }

    @Test
    public void descriptorPreservesLongListLength() {
        ListNBT timestamps = new ListNBT();
        timestamps.add(LongNBT.valueOf(123L));
        List<Object> converted = NBT2Collection.toList(timestamps);
        assertEquals(Arrays.asList(123L), converted);
    }
}
