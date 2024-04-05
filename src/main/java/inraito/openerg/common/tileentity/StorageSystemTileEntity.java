package inraito.openerg.common.tileentity;

import inraito.openerg.api.StorageSystem;
import inraito.openerg.common.tileentity.storage.StorageStaticLib;
import inraito.openerg.common.tileentity.storage.StorageSystemReceiver;
import inraito.openerg.common.tileentity.storage.StorageSystemSender;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class StorageSystemTileEntity extends TileEntityEnvironment
        implements StorageSystem {
    protected final StorageSystemReceiver storageWrapper = new StorageSystemReceiver(this);

    public StorageSystemTileEntity(TileEntityType<?> type) {
        super(type);
    }

    /*
    --------------------------------------------------------Environment------------------------------------------------
     */
    //TODO: using stack instead of a single variable.
    protected StorageSystemSender sender = null;
    public void setSender(StorageSystemSender sender) {
        this.sender = sender;
    }

    @Override
    public void onMessage(Message message) {
        Object wrapperRes = this.storageWrapper.onMessage(message);
        if(wrapperRes!=null){
            this.node.network().sendToAddress(this.node,
                    message.source().address(),
                    StorageStaticLib.RESPONSE_NAME,
                    wrapperRes);
            return;
        }
        if(sender!=null && sender.onMessage(message)){
            return;
        }
        super.onMessage(message);
    }
}
