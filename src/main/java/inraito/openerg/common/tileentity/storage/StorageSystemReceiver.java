package inraito.openerg.common.tileentity.storage;

import inraito.openerg.api.StorageSystem;
import li.cil.oc.api.network.Message;
import net.minecraft.item.ItemStack;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;

/**
 *
 */
public class StorageSystemReceiver {

    private final StorageSystem storage;

    public StorageSystemReceiver(StorageSystem storageSystem){
        this.storage = storageSystem;
    }

    public Object onMessage(Message message) {
        if(!message.name().equals(REQUEST_NAME)){
            return null;
        }
        try{
            return handle(message.data());
        }catch (Exception e){
            return null;
        }
    }

    private Object handle(Object[] data){
        String method = ((String) data[0]);
        switch (method) {
            case GET_SLOT_NUM:
                return storage.getSlotNum();
            case PUSH:
                return storage.push(((Integer) data[1]), ((ItemStack) data[2]), ((boolean) data[3]));
            case POP:
                return storage.pop(((Integer) data[1]), ((Integer) data[2]));
            case CHECK:
                return storage.check(((Integer) data[1]));
        }
        return null;
    }
}
