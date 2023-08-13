package inraito.openerg.common.container;

import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.items.SlotItemHandler;

public class OCInterfaceContainer extends Container {

    /**
     * used only on client, presumably.
     */
    protected OCInterfaceContainer(int pContainerId) {
        super(ContainerList.ocInterfaceContainer.get(), pContainerId);
    }

    /**
     * used on server.
     * @param tileEntity tileentity of the OCInterface.
     */
    public OCInterfaceContainer(int pContainerId, OCInterfaceTileEntity tileEntity) {
        super(ContainerList.ocInterfaceContainer.get(), pContainerId);
    }

    @Override
    public boolean stillValid(PlayerEntity pPlayer) {
        return true;
    }
}
