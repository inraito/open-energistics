package inraito.openerg.common.driver;

import inraito.openerg.HeadlessItems;
import inraito.openerg.common.driver.DriverMESwitchingCard.MESwitchingCardEnvironment;
import li.cil.oc.api.API;
import li.cil.oc.api.detail.NetworkAPI;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MESwitchingCardTest extends HeadlessItems {
    private final NetworkAPI previous = API.network;
    private final Arguments args = mock(Arguments.class);
    private MESwitchingCardEnvironment card;

    @Before
    public void connectCard() {
        API.network = mock(NetworkAPI.class, RETURNS_DEEP_STUBS);
        card = new MESwitchingCardEnvironment();
        when(args.checkString(0)).thenReturn("source");
        when(args.checkInteger(1)).thenReturn(2);
        when(args.checkInteger(2)).thenReturn(3);
    }

    @After
    public void restoreNetwork() {
        API.network = previous;
    }

    private CompoundNBT savedStack() {
        ListNBT stack = new ListNBT();
        stack.add(new ItemStack(Items.IRON_INGOT, 3).save(new CompoundNBT()));
        CompoundNBT state = new CompoundNBT();
        state.put("stack_data", stack);
        return state;
    }

    private Message reply(Object value) {
        Message message = mock(Message.class);
        when(message.name()).thenReturn(RESPONSE_NAME);
        when(message.data()).thenReturn(new Object[]{value});
        return message;
    }

    @Test
    public void restoresCountAndRejectsOverCapacity() throws Exception {
        CompoundNBT saved = savedStack();
        ListNBT stack = (ListNBT) saved.get("stack_data");
        for (int i = 1; i < 5; i++) stack.add(stack.getCompound(0).copy());
        card.loadData(saved);
        assertEquals(3, ((Map<?, ?>) card.peekTop(null, args)[0]).get("num"));
        assertArrayEquals(new Object[]{null, "stack is full"}, card.push(null, args));
    }

    @Test
    public void registryNameInLuaDescriptorIsAString() throws Exception {
        card.loadData(savedStack());
        Object[] converted = li.cil.oc.server.driver.Registry$.MODULE$.convert(card.peekTop(null, args));
        scala.collection.Map<?, ?> descriptor = (scala.collection.Map<?, ?>) converted[0];
        assertEquals("minecraft:iron_ingot", scala.jdk.javaapi.CollectionConverters.asJava(descriptor).get("registry_name"));
    }

    @Test
    public void repeatedLoadReplacesContents() {
        CompoundNBT state = savedStack();
        card.loadData(state);
        card.loadData(state);
        CompoundNBT saved = new CompoundNBT();
        card.saveData(saved);
        assertEquals(1, ((ListNBT) saved.get("stack_data")).size());
    }

    @Test
    public void successfulResponsePopulatesCard() throws Exception {
        Node node = card.node();
        li.cil.oc.api.network.Network network = node.network();
        Message valid = reply(new ItemStack(Items.IRON_INGOT, 3));
        doAnswer(call -> { card.onMessage(valid); return null; })
                .when(network).sendToAddress(node, "source", REQUEST_NAME, POP, 2, 3);
        assertEquals(true, card.push(null, args)[0]);
        assertEquals(3, ((Map<?, ?>) card.peekTop(null, args)[0]).get("num"));
    }

    @Test
    public void validRequestRecoversAfterMalformedReply() throws Exception {
        Node node = card.node();
        li.cil.oc.api.network.Network network = node.network();
        Message malformed = reply("wrong payload");
        Message valid = reply(new ItemStack(Items.IRON_INGOT, 3));
        doAnswer(call -> { card.onMessage(malformed); return null; })
                .doAnswer(call -> { card.onMessage(valid); return null; })
                .when(network).sendToAddress(node, "source", REQUEST_NAME, POP, 2, 3);
        assertEquals(false, card.push(null, args)[0]);
        assertEquals(true, card.push(null, args)[0]);
    }

    @Test
    public void receiverRefusalKeepsCardTop() throws Exception {
        card.loadData(savedStack());
        // No reply from the destination.
        assertEquals(false, card.pop(null, args)[0]);
        assertEquals(3, ((Map<?, ?>) card.peekTop(null, args)[0]).get("num"));
    }
}
