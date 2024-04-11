package inraito.openerg.common.tileentity;

import inraito.openerg.util.IndexMapOnFS;
import li.cil.oc.api.API;
import li.cil.oc.api.fs.FileSystem;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.integration.opencomputers.DriverFileSystem$;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import scala.Tuple3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Block Controller, note that the slot index may not be continuous.
 */
public class BlockControllerTileEntity extends StorageSystemTileEntity
        implements ITickableTileEntity, EnvironmentHost {

    public final ItemStackHandler fsSlot = new ItemStackHandler(1){
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            DriverItem driver = DriverFileSystem$.MODULE$;
            return driver.worksWith(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            BlockControllerTileEntity.this.setChanged();
            if(this.getStackInSlot(0).isEmpty()){
                BlockControllerTileEntity.this.environment.node().remove();
                BlockControllerTileEntity.this.environment = null;
            }
        }
    };

    public BlockControllerTileEntity() {
        super(TileEntityList.blockControllerTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("block_controller").create();
    }

    LazyOptional<IItemHandler> fsInventory = LazyOptional.of(()->this.fsSlot);
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap== CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return fsInventory.cast();
        }
        return super.getCapability(cap, side);
    }

    li.cil.oc.server.component.FileSystem environment = null;

    @Nullable
    private FileSystem fs(){
        ItemStack stack = fsSlot.getStackInSlot(0);
        if(stack.isEmpty() || !DriverFileSystem$.MODULE$.worksWith(stack)){
            return null;
        }
        if(environment == null) {
            throw new IllegalStateException("environment is null!");
        }
        return environment.fileSystem();
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        nbt.put("fs", fsSlot.serializeNBT());
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        fsSlot.deserializeNBT(nbt.getCompound("fs"));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(node!=null){
            node.remove();
        }
        if(this.environment!=null){
            this.environment.node().remove();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(node!=null){
            node.remove();
        }
        if(this.environment!=null){
            this.environment.node().remove();
        }
    }

    /*
    ---------------------------------------------------------Tick------------------------------------------------------
     */

    @Override
    public void tick() {
        if (node != null && node.network() == null) {
            API.network.joinOrCreateNetwork(this);
        }
        if(this.environment == null && !this.fsSlot.getStackInSlot(0).isEmpty()){
            this.environment = ((li.cil.oc.server.component.FileSystem)
                    DriverFileSystem$.MODULE$.createEnvironment(this.fsSlot.getStackInSlot(0), this));
            this.node.connect(this.environment.node());
        }
    }

    /*
    -----------------------------------------------------StorageSystem-------------------------------------------------
     */

    private IItemHandler getItemHandler(Direction relative, Direction side){
        try {
            BlockPos pos = this.getBlockPos().relative(relative);
            TileEntity tileEntity = this.level.getBlockEntity(pos);
            LazyOptional<IItemHandler> handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            return handler.resolve().get();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int getSlotNum() {
        //Not supported, and it's probably useless actually.
        //Could be implemented by adding more control data into the fs, but i doubt it worth it.
        return -1;
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack, boolean atomic) {
        itemStack = itemStack.copy();
        try{
            Tuple3<Direction, Direction, Integer> tuple = IndexMapOnFS.get(fs(), slot);
            IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
            if(atomic){
                ItemStack temp = handler.insertItem(tuple._3(), itemStack, true);
                if(temp.isEmpty()){
                    temp = handler.insertItem(tuple._3(), itemStack, false);
                    if(!temp.isEmpty()){
                        throw new IllegalStateException("unknown bug");
                    }else{
                        return temp;
                    }
                }
                return itemStack;
            }else {
                return handler.insertItem(tuple._3(), itemStack, false);
            }
        }catch (Exception e){
            return itemStack;
        }
    }

    @Override
    public ItemStack pop(int slot, int num) {
        Tuple3<Direction, Direction, Integer> tuple = IndexMapOnFS.get(fs(), slot);
        try{
            IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
            return handler.extractItem(tuple._3(), num, false).copy();
        }catch (Exception e){
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack check(int slot) {
        Tuple3<Direction, Direction, Integer> tuple = IndexMapOnFS.get(fs(), slot);
        try{
            IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
            return handler.getStackInSlot(tuple._3()).copy();
        }catch (Exception e){
            return ItemStack.EMPTY;
        }
    }

    /*
    --------------------------------------------------------Environment------------------------------------------------
     */

    @Override
    public void onMessage(Message message) {
        super.onMessage(message);
    }

    /*
    ----------------------------------------------OpenComputers Component Callbacks------------------------------------
     */

    @Callback(doc="function(index:int, relative:string, side:string, slot:int):boolean -- map the given index in storage system to the given slot")
    public Object[] map(Context context, Arguments arguments) throws Exception{
        int index = arguments.checkInteger(0);
        Direction relative = Direction.byName(arguments.checkString(1));
        Direction side = Direction.byName(arguments.checkString(2));
        int slot = arguments.checkInteger(3);
        Tuple3<Direction, Direction, Integer> tuple = new Tuple3<>(relative, side, slot);
        if(IndexMapOnFS.containsKey(fs(), index)){
            return new Object[]{false};
        }
        IndexMapOnFS.put(fs(), index, tuple);
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function(index:int):boolean -- unmap the given index in storage system")
    public Object[] unmap(Context context, Arguments arguments) throws Exception{
        int index = arguments.checkInteger(0);
        if(!IndexMapOnFS.containsKey(fs(), index)){
            return new Object[]{false};
        }
        IndexMapOnFS.remove(fs(), index);
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function(index:int):table -- get all mappings")
    public Object[] check(Context context, Arguments arguments) throws Exception{
        int index = arguments.checkInteger(0);
        Tuple3<Direction, Direction, Integer> tuple = IndexMapOnFS.get(fs(), index);
        if(tuple==null){
            return new Object[]{null};
        }else{
            Map<String, Object> res = new HashMap<>();
            res.put("relative", tuple._1().toString());
            res.put("side", tuple._2().toString());
            res.put("slot", tuple._3());
            return new Object[]{res};
        }
    }

    /*
    ----------------------------------------------OpenComputers Environment Host---------------------------------------
     */

    @Override
    public World world() {
        return this.getLevel();
    }

    @Override
    public double xPosition() {
        return this.getBlockPos().getX() + 0.5;
    }

    @Override
    public double yPosition() {
        return this.getBlockPos().getY() + 0.5;
    }

    @Override
    public double zPosition() {
        return this.getBlockPos().getZ() + 0.5;
    }

    @Override
    public void markChanged() {
        this.setChanged();
    }
}
