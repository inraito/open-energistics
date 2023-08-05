package inraito.openerg.common.tileentity;

import appeng.api.networking.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.util.Platform;
import inraito.openerg.common.item.ItemList;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class OCInterfaceTileEntity extends TileEntity implements IGridHost, IGridBlock {
    public OCInterfaceTileEntity() {
        super(TileEntityList.ocInterfaceTileEntity.get());
    }

    private IGridNode node;

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        if (this.node == null && Platform.isServer()) {
            this.node = Api.instance().grid().createGridNode(this);
            this.node.updateState();
        }
        return this.node;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    public void securityBreak() {
        this.getLevel().setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
    }

    @Override
    public double getIdlePowerUsage() {
        return 0.5;
    }

    @Nonnull
    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Nonnull
    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(@Nonnull GridNotification notification) {

    }

    @Nonnull
    @Override
    public EnumSet<Direction> getConnectableSides() {
        return EnumSet.allOf(Direction.class);
    }

    @Nonnull
    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public void gridChanged() {

    }

    @Nonnull
    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(ItemList.ocInterfaceItem.get());
    }
}
