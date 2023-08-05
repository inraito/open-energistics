package inraito.openerg.common.driver;

import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OCInterfaceDriver extends DriverSidedTileEntity {
    protected static class OCInterfaceEnvironment extends AbstractManagedEnvironment {
        OCInterfaceTileEntity tileEntity;

        public OCInterfaceEnvironment(OCInterfaceTileEntity tileEntity){
            this.tileEntity = tileEntity;
            setNode(Network.newNode(this, Visibility.Network)
                    .withComponent("oc_interface").create());
        }
    }

    @Override
    public Class<?> getTileEntityClass() {
        return OCInterfaceTileEntity.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side) {
        return new OCInterfaceDriver.OCInterfaceEnvironment(((OCInterfaceTileEntity) world.getBlockEntity(pos)));
    }
}
