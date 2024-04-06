package inraito.openerg.common.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import scala.Tuple3;

import java.util.HashMap;
import java.util.Map;

/**
 * Block Controller, note that the slot index may not be continuous.
 */
public class BlockControllerTileEntity extends StorageSystemTileEntity
        implements ITickableTileEntity {

    public BlockControllerTileEntity() {
        super(TileEntityList.blockControllerTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("block_controller").create();
    }

    private final Map<Integer, Tuple3<Direction, Direction, Integer>> mapping = new HashMap<>();

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        ListNBT list = new ListNBT();
        for(Map.Entry<Integer, Tuple3<Direction, Direction, Integer>> entry : mapping.entrySet()){
            CompoundNBT c = new CompoundNBT();
            Integer index = entry.getKey();
            Tuple3<Direction, Direction, Integer> tuple = entry.getValue();
            c.putInt("index", index);
            c.putString("relative", tuple._1().getName());
            c.putString("side", tuple._2().getName());
            c.putInt("slot", tuple._3());
            list.add(c);
        }
        nbt.put("entries", list);
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        ListNBT list = ((ListNBT) nbt.get("entries"));
        for(INBT element : list){
            CompoundNBT c = ((CompoundNBT) element);
            Integer index = c.getInt("index");
            Direction relative = Direction.byName(c.getString("relative"));
            Direction side = Direction.byName(c.getString("side"));
            Integer slot = c.getInt("slot");
            Tuple3<Direction, Direction, Integer> tuple = new Tuple3<>(relative, side, slot);
            this.mapping.put(index, tuple);
        }
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

    private IItemHandler getItemHandler(Direction relative, Direction side){
        try {
            BlockPos pos = this.getBlockPos().relative(relative);
            TileEntity tileEntity = this.level.getBlockEntity(pos);
            LazyOptional<IItemHandler> handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            return handler.resolve().get();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int getSlotNum() {
        return this.mapping.size();
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack) {
        Tuple3<Direction, Direction, Integer> tuple = this.mapping.get(slot);
        IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
        try{
            return handler.insertItem(slot, itemStack, false);
        }catch (Exception e){
            return itemStack;
        }
    }

    @Override
    public ItemStack pop(int slot, int num) {
        Tuple3<Direction, Direction, Integer> tuple = this.mapping.get(slot);
        IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
        try{
            return handler.extractItem(slot, num, false);
        }catch (Exception e){
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack check(int slot) {
        Tuple3<Direction, Direction, Integer> tuple = this.mapping.get(slot);
        IItemHandler handler = this.getItemHandler(tuple._1(), tuple._2());
        try{
            return handler.getStackInSlot(slot).copy();
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

    @Callback(doc="function(index:int, relative:string, side:string, slot:int):boolean --map the given index in storage system to the given slot")
    public Object[] map(Context context, Arguments arguments) throws Exception{
        int index = arguments.checkInteger(0);
        Direction relative = Direction.byName(arguments.checkString(1));
        Direction side = Direction.byName(arguments.checkString(2));
        int slot = arguments.checkInteger(3);
        Tuple3<Direction, Direction, Integer> tuple = new Tuple3<>(relative, side, slot);
        if(this.mapping.containsKey(index)){
            return new Object[]{false};
        }
        this.mapping.put(index, tuple);
        return new Object[]{true};
    }

    @Callback(doc="function(index:int):boolean --unmap the given index in storage system")
    public Object[] unmap(Context context, Arguments arguments) throws Exception{
        int index = arguments.checkInteger(0);
        if(!this.mapping.containsKey(index)){
            return new Object[]{false};
        }
        this.mapping.remove(index);
        return new Object[]{true};
    }
}
