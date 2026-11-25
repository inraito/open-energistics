package inraito.openerg.common.tileentity.storage;

import inraito.openerg.common.tileentity.StorageSystemTileEntity;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Network;
import li.cil.oc.api.network.Node;
import org.junit.Before;
import org.junit.Test;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StorageSystemSenderTest {
    private final StorageSystemTileEntity tile = mock(StorageSystemTileEntity.class);
    private final Node node = mock(Node.class);
    private final Network network = mock(Network.class);
    private final StorageSystemSender sender = new StorageSystemSender("disk", tile);

    @Before
    public void connect() {
        when(tile.node()).thenReturn(node);
        when(node.network()).thenReturn(network);
    }

    private Message reply(String addr) {
        Message reply = mock(Message.class);
        Node source = mock(Node.class);
        when(source.address()).thenReturn(addr);
        when(reply.source()).thenReturn(source);
        when(reply.name()).thenReturn(RESPONSE_NAME);
        when(reply.data()).thenReturn(new Object[]{17});
        return reply;
    }

    @Test
    public void timeoutAfterValidReplyUsesFallback() {
        Message reply = reply("disk");
        doAnswer(call -> { sender.onMessage(reply); return null; }).doNothing()
                .when(network).sendToAddress(node, "disk", REQUEST_NAME, GET_SLOT_NUM);
        assertEquals(17, sender.getSlotNum());
        assertEquals(0, sender.getSlotNum());
    }

    @Test
    public void unrelatedNodeCannotAnswerForTheAddressedStorage() {
        Message reply = reply("other");
        doAnswer(call -> { sender.onMessage(reply); return null; })
                .when(network).sendToAddress(node, "disk", REQUEST_NAME, GET_SLOT_NUM);
        assertEquals(0, sender.getSlotNum());
    }

    @Test
    public void missingReplyUsesFallback() {
        assertEquals(0, sender.getSlotNum());
        verify(tile).setSender(sender);
    }
}
