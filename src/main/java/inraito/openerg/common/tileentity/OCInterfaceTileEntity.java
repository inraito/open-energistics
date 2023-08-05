package inraito.openerg.common.tileentity;

import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.util.Platform;
import inraito.openerg.common.item.ItemList;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class OCInterfaceTileEntity extends TileEntityEnvironment implements IGridHost,
        IGridBlock, ICraftingProvider {
    public OCInterfaceTileEntity() {
        super(TileEntityList.ocInterfaceTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("oc_interface").create();
    }

    private IGridNode aeNode;

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        if (this.aeNode == null && Platform.isServer()) {
            this.aeNode = Api.instance().grid().createGridNode(this);
            this.aeNode.updateState();
        }
        return this.aeNode;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.GLASS;
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

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        //TODO
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
        //TODO
        return false;
    }

    @Override
    public boolean isBusy() {
        //TODO
        return false;
    }
}
