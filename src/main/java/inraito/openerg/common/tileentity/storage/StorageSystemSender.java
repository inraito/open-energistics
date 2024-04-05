package inraito.openerg.common.tileentity.storage;

import inraito.openerg.api.StorageSystem;
import inraito.openerg.common.tileentity.StorageSystemTileEntity;
import li.cil.oc.api.network.Environment;
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

    Message response = null;
    public boolean onMessage(Message message){
        if(message.name().equals(RESPONSE_NAME)){
            this.response = message;
            return true;
        }
        return false;
    }

    @Override
    public int getSlotNum() {
        try {
            Node node = environment.node();
            environment.setSender(this);
            node.network().sendToAddress(node, address, REQUEST_NAME,
                    GET_SLOT_NUM);
            return ((Integer) response.data()[0]);
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack) {
        try {
            Node node = environment.node();
            environment.setSender(this);
            node.network().sendToAddress(node, address, REQUEST_NAME,
                    PUSH, slot, itemStack);
            return ((ItemStack) response.data()[0]);
        }catch (Exception e){
            return itemStack;
        }
    }

    @Override
    public ItemStack pop(int slot, int num) {
        try {
            Node node = environment.node();
            environment.setSender(this);
            node.network().sendToAddress(node, address, REQUEST_NAME,
                    POP, slot, num);
            return ((ItemStack) response.data()[0]);
        }catch (Exception e){
            return ItemStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public ItemStack check(int slot) {
        try {
            Node node = environment.node();
            environment.setSender(this);
            node.network().sendToAddress(node, address, REQUEST_NAME,
                    CHECK, slot);
            return ((ItemStack) response.data()[0]);
        }catch (Exception e){
            return null;
        }
    }
}
