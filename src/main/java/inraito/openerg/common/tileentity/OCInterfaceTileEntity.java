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
import inraito.openerg.Lib;
import inraito.openerg.common.container.OCInterfaceContainer;
import inraito.openerg.common.item.ItemList;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class OCInterfaceTileEntity extends TileEntityEnvironment implements IGridHost,
        IGridBlock, ICraftingProvider, INamedContainerProvider {
    private static final String CONFIG_INVENTORY_KEY = "config_inventory";
    private static final String STORAGE_INVENTORY_KEY = "storage_inventory";

    //slot used to config this interface, i.e. using ae crafting patterns to add more recipes
    ItemStackHandler configInventory = new ItemStackHandler(1);
    //slots used to cache output(i/o all from the perspective of an ae network)
    //and input should not be send here, which is not similar to a me interface
    ItemStackHandler storageInventory = new ItemStackHandler(27);
    public OCInterfaceTileEntity() {
        super(TileEntityList.ocInterfaceTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("oc_interface").create();
        configInventoryLazyOptional = LazyOptional.of(()->this.configInventory);
        storageInventoryLazyOptional = LazyOptional.of(()->this.storageInventory);
    }

    LazyOptional<IItemHandler> configInventoryLazyOptional;
    LazyOptional<IItemHandler> storageInventoryLazyOptional;
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        //config is only available on the top, but i doubt that there exist any usages of that, just in case.
        if(cap== CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(side==Direction.UP){
                return configInventoryLazyOptional.cast();
            }else{
                return storageInventoryLazyOptional.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put(CONFIG_INVENTORY_KEY, configInventory.serializeNBT());
        nbt.put(STORAGE_INVENTORY_KEY, storageInventory.serializeNBT());
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        configInventory.deserializeNBT(nbt.getCompound(CONFIG_INVENTORY_KEY));
        storageInventory.deserializeNBT(nbt.getCompound(STORAGE_INVENTORY_KEY));
    }

    /*
    --------------------------------------------INamedContainerProvider---------------------------------------------
     */

    private static final String CONTAINER_DISPLAY_NAME_KEY = Lib.modid + ".oc_interface.container.display_name";
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(CONTAINER_DISPLAY_NAME_KEY);
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new OCInterfaceContainer(id, this);
    }

    /*
     --------------------------------------------------AE Grid---------------------------------------------------------
     */

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

    /*
     ----------------------------------------------------ICraftingProvider-------------------------------------------
     */

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
