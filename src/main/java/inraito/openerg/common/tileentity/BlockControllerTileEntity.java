package inraito.openerg.common.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Visibility;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;

public class BlockControllerTileEntity extends StorageSystemTileEntity
        implements ITickableTileEntity {

    public BlockControllerTileEntity() {
        super(TileEntityList.blockControllerTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("block_controller").create();
    }

    /*
    ---------------------------------------------------------Tick------------------------------------------------------
     */

    @Override
    public void tick() {

    }

    /*
    -----------------------------------------------------StorageSystem-------------------------------------------------
     */

    @Override
    public int getSlotNum() {
        return 0;
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack) {
        return null;
    }

    @Override
    public ItemStack pop(int slot, int num) {
        return null;
    }

    @Override
    public ItemStack check(int slot) {
        return null;
    }

    /*
    --------------------------------------------------------Environment------------------------------------------------
     */

    @Override
    public void onMessage(Message message) {
        super.onMessage(message);
    }
}
