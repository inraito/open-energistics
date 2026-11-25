package inraito.openerg.common.tileentity.storage;

import inraito.openerg.api.StorageSystem;
import li.cil.oc.api.network.Message;
import org.junit.Test;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StorageSystemReceiverTest {
    private final StorageSystem inventory = mock(StorageSystem.class);
    private final StorageSystemReceiver receiver = new StorageSystemReceiver(inventory);

    private Message message(String name, Object... data) {
        Message message = mock(Message.class);
        when(message.name()).thenReturn(name);
        when(message.data()).thenReturn(data);
        return message;
    }

    @Test
    public void dispatchesRequestsWithTheirSlotCountAndAtomicFlag() {
        when(inventory.getSlotNum()).thenReturn(15);
        assertEquals(15, receiver.onMessage(message(REQUEST_NAME, GET_SLOT_NUM)));
        receiver.onMessage(message(REQUEST_NAME, CHECK, 7));
        receiver.onMessage(message(REQUEST_NAME, POP, 7, 3));
        receiver.onMessage(message(REQUEST_NAME, PUSH, 9, null, true));
        verify(inventory).check(7);
        verify(inventory).pop(7, 3);
        verify(inventory).push(9, null, true);
    }

    @Test
    public void ignoresUnrelatedUnknownAndMalformedRequests() {
        assertNull(receiver.onMessage(message(RESPONSE_NAME, GET_SLOT_NUM)));
        assertNull(receiver.onMessage(message(REQUEST_NAME, "unknown")));
        assertNull(receiver.onMessage(message(REQUEST_NAME, POP, 1)));
        assertNull(receiver.onMessage(message(REQUEST_NAME, CHECK, "seven")));
        verifyNoInteractions(inventory);
    }
}
