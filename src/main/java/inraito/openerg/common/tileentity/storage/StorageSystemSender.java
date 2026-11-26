package inraito.openerg.common.tileentity.storage;

import inraito.openerg.api.StorageSystem;
import inraito.openerg.common.tileentity.StorageSystemTileEntity;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;

public class StorageSystemSender implements StorageSystem {
    private final String address;
    private final StorageSystemTileEntity environment;

    public StorageSystemSender(String address, StorageSystemTileEntity environment){
        this.address = address;
        this.environment = environment;
    }

    private Message response;
    private boolean waiting;

    public boolean onMessage(Message message) {
        if (waiting && RESPONSE_NAME.equals(message.name()) && address.equals(message.source().address())) {
            response = message;
            return true;
        }
        return false;
    }

    private Object request(Object... data) {
        Node node = environment.node();
        waiting = true;
        environment.setSender(this);
        try {
            node.network().sendToAddress(node, address, REQUEST_NAME, data);
            return response == null ? null : response.data()[0];
        } finally {
            environment.setSender(null);
            waiting = false;
            response = null;
        }
    }

    @Override
    public int getSlotNum() {
        try {
            return (Integer) request(GET_SLOT_NUM);
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack, boolean atomic) {
        try{
            ItemStack remainder = (ItemStack) request(PUSH, slot, itemStack, atomic);
            return remainder == null ? itemStack : remainder;
        }catch (Exception e){
            return itemStack;
        }
    }

    @Override
    public ItemStack pop(int slot, int num) {
        try {
            ItemStack extracted = (ItemStack) request(POP, slot, num);
            return extracted == null ? ItemStack.EMPTY : extracted;
        }catch (Exception e){
            return ItemStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public ItemStack check(int slot) {
        try {
            return (ItemStack) request(CHECK, slot);
        }catch (Exception e){
            return null;
        }
    }
}
