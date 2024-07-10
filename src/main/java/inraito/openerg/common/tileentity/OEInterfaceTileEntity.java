package inraito.openerg.common.tileentity;

import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.util.Platform;
import inraito.openerg.common.Config;
import inraito.openerg.common.block.BlockList;
import inraito.openerg.common.item.ItemList;
import inraito.openerg.util.ItemHandlerHelper;
import inraito.openerg.util.nbt.NBT2Collection;
import li.cil.oc.api.API;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class OEInterfaceTileEntity extends StorageSystemTileEntity implements IGridHost,
        IGridBlock, ICraftingProvider, ITickableTileEntity {
    public static final String MSG_PREFIX = "oe_interface:";
    public static final int MAX_STORAGE_SIZE = 32;//TODO: use config instead

    public ItemStackHandler configInventory = new ItemStackHandler(1){
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            //only encoded patterns are accepted
            return Api.instance().crafting().isEncodedPattern(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            OEInterfaceTileEntity.this.setChanged();
        }
    };

    LazyOptional<IItemHandler> configInventoryLazyOptional;
    public OEInterfaceTileEntity() {
        super(TileEntityList.oeInterfaceTileEntity.get());
        super.node = Network.newNode(this, Visibility.Network).
                withComponent("oe_interface").create();
        configInventoryLazyOptional = LazyOptional.of(()->this.configInventory);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap== CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return configInventoryLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        CompoundNBT tag = super.save(nbt);
        tag.putInt("sequence_head", sequenceHead);
        tag.putInt("sequence_tail", sequenceTail);
        tag.putInt("port", this.port);
        tag.put("config_inventory", this.configInventory.serializeNBT());

        ListNBT list = new ListNBT();
        this.storage.forEach(pattern->{
            ListNBT l = new ListNBT();
            pattern.forEach(stack->l.add(stack.save(new CompoundNBT())));
            list.add(l);
        });
        tag.put("storage", list);

        saveCraftingPatterns(tag);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        this.sequenceHead = nbt.getInt("sequence_head");
        this.sequenceTail = nbt.getInt("sequence_tail");
        this.port = nbt.getInt("port");
        this.configInventory.deserializeNBT(nbt.getCompound("config_inventory"));

        this.storage.clear();
        ListNBT list = ((ListNBT) nbt.get("storage"));
        List<Object> storage = NBT2Collection.toList(list);
        storage.forEach(pattern->{
            List<ItemStack> p = new ArrayList<>();
            List<CompoundNBT> l = (List<CompoundNBT>) pattern;
            l.forEach(tag-> p.add(ItemStack.of(tag)));
            this.storage.add(p);
        });

        loadCraftingPatterns(nbt);
        super.load(state, nbt);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        this.load(state, tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        IGridNode node1 = this.getGridNode(AEPartLocation.INTERNAL);
        if (node1 != null){
            node1.destroy();
        }
        if(node!=null){
            node.remove();
        }
    }

    public void onRemove(BlockState state){
        if (state.getBlock() != BlockList.oeInterface.get()) {
            this.getGridNode(AEPartLocation.INTERNAL).destroy();
            BlockPos blockPos = this.getBlockPos();
            Vector3d pos = new Vector3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            ItemHandlerHelper.dropContents(this.configInventory, this.level, pos);
            this.storage.forEach(pattern -> pattern.forEach(itemStack ->
                    ItemHandlerHelper.dropItemStack(this.level, pos, itemStack)
            ));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(node!=null){
            node.remove();
        }
    }

    /*
    -----------------------------------------------------Tick-------------------------------------------------------
     */

    private boolean nodeUpdated = false;
    @Override
    public void tick() {
        if (node != null && node.network() == null) {
            API.network.joinOrCreateNetwork(this);
        }
        //initialize the ae node, this is here because it seems i can't do that in onLoad()
        if(!nodeUpdated){
            this.getGridNode(AEPartLocation.INTERNAL);
            nodeUpdated = true;
        }
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
        return new ItemStack(ItemList.oeInterfaceItem.get());
    }

    /*
     ----------------------------------------------------ICraftingProvider-------------------------------------------
     */
    private void saveCraftingPatterns(CompoundNBT nbt){
        ListNBT list = new ListNBT();
        for(ItemStack stack : craftingPatterns.keySet()){
            CompoundNBT entry = new CompoundNBT();
            CompoundNBT stackCompound = new CompoundNBT();
            stack.save(stackCompound);
            entry.put("stack", stackCompound);
            entry.putString("id", craftingPatterns.get(stack));
            list.add(entry);
        }
        nbt.put("crafting_patterns", list);
    }

    private void loadCraftingPatterns(CompoundNBT nbt){
        ListNBT list = ((ListNBT) nbt.get("crafting_patterns"));
        for(int i=0;i<list.size();i++){
            CompoundNBT entry = list.getCompound(i);
            ItemStack stack = ItemStack.of(entry.getCompound("stack"));
            String id = entry.getString("id");
            this.craftingPatterns.put(stack, id);
        }
    }

    private final Map<ItemStack, String> craftingPatterns = new HashMap<>();

    private int port = 2048;

    //head and tail are more like a window. only when the first crafting job is finished,
    //will the head be moved forward.
    //
    //I doubt that anyone would exhaust 2^32 in a single minecraft save.
    //If that is the case, you could use resetSequence callback to reset head and tail to 0.
    private int sequenceHead = 0;
    private int sequenceTail = 0;
    private void broadcastMessage(String id, int length){
        int start = sequenceTail;
        int end = start + length;
        sequenceTail = end;
        Packet packet = Network.newPacket(this.node.address(), null, port,
                new Object[]{MSG_PREFIX + id, start, end});//start inclusive, end exclusive
        this.node().network().sendToReachable(this.node(), "network.message", packet);
    }

    private final Map<ICraftingPatternDetails, ItemStack> cachedDetails = new HashMap<>();
    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        this.cachedDetails.clear();
        for(ItemStack stack : craftingPatterns.keySet()){
            ICraftingPatternDetails details = Api.instance().crafting().decodePattern(stack, this.level);
            cachedDetails.put(details, stack);
            craftingTracker.addCraftingOption(this, details);
        }
    }

    private final List<List<ItemStack>> storage = new LinkedList<>();

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
        if(this.storage.size()>=MAX_STORAGE_SIZE){
            return false;
        }
        int size = table.getContainerSize();
        List<ItemStack> node = new ArrayList<>();
        for(int i=0;i<size;i++){
            ItemStack stack = table.getItem(i);
            if(!stack.isEmpty()){
                node.add(stack);
            }
        }
        this.storage.add(node);
        String id = craftingPatterns.get(cachedDetails.get(patternDetails));
        broadcastMessage(id, node.size());
        this.setChanged();
        return true;
    }

    @Override
    public boolean isBusy() {
        return this.storage.size()>=MAX_STORAGE_SIZE;
    }

    /*
    -------------------------------------OpenComputers Component Callbacks------------------------------------------
     */

    @Callback(doc="function(message:string):boolean --register the pattern in the config slot with the given message, return true if succeed")
    public Object[] registerPattern(Context context, Arguments arguments) throws Exception{
        String id = arguments.checkString(0);
        if(id.isEmpty()){
            throw new IllegalArgumentException();
        }
        if(this.configInventory.getStackInSlot(0).isEmpty()){
            return new Object[]{false, "config slot empty"};
        }
        if(this.craftingPatterns.containsValue(id)){
            return new Object[]{false, "id has been used"};
        }
        if(craftingPatterns.keySet().size()>=Config.MAXIMUM_PATTERNS.get()){
            return new Object[]{false, "pattern number reach limit"};
        }
        ItemStack pattern = this.configInventory.getStackInSlot(0);
        if(Api.instance().crafting().decodePattern(pattern, this.level)==null){
            return new Object[]{false, "pattern not valid"};
        }
        this.craftingPatterns.put(pattern, id);
        this.aeNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.aeNode));
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function():table --list all registered patterns, only the registered messages are returned")
    public Object[] listAllPatterns(Context context, Arguments arguments) throws Exception{
        List<String> res = new ArrayList<>(craftingPatterns.values());
        return new Object[]{res};
    }

    @Callback(doc="function(message:string):table, table --get pattern details, output first, input follows")
    public Object[] getPattern(Context context, Arguments arguments) throws Exception{
        String msg = arguments.checkString(0);
        Optional<Map.Entry<ItemStack, String>> entry = craftingPatterns.entrySet()
                .stream().filter((e)->e.getValue().equals(msg))
                .findFirst();
        if(!entry.isPresent()){
            return new Object[]{null, "pattern not found"};
        }
        ItemStack stack = entry.get().getKey();
        ICraftingPatternDetails details = Api.instance().crafting().decodePattern(stack, this.getLevel());
        List<String> inputs = new ArrayList<>();
        List<String> outputs = new ArrayList<>();
        for(IAEItemStack aeStack : details.getInputs()){
            String id = aeStack.getItem().getDescriptionId();
            inputs.add(id);
        }
        for(IAEItemStack aeStack : details.getOutputs()){
            String id = aeStack.getItem().getDescriptionId();
            outputs.add(id);
        }
        return new Object[]{outputs, inputs};
    }

    @Callback(doc="function(port:number):boolean --set broadcast port")
    public Object[] setPort(Context context, Arguments arguments) throws Exception{
        this.port = arguments.checkInteger(0);
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function(message:string):boolean --delete registered pattern")
    public Object[] deletePattern(Context context, Arguments arguments) throws Exception{
        String id = arguments.checkString(0);
        Optional<ItemStack> optional = this.craftingPatterns.keySet().stream().filter(k->craftingPatterns.get(k)
                .equals(id)).findFirst();
        if(!optional.isPresent()){
            return new Object[]{false, "massage not registered"};
        }
        this.craftingPatterns.remove(optional.get());
        this.aeNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.aeNode));
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function():boolean --reset the sequence")
    public Object[] resetSequence(Context context, Arguments arguments) throws Exception{
        if(sequenceHead!=sequenceTail){
            return new Object[]{false, "not empty"};
        }
        this.sequenceHead = 0;
        this.sequenceTail = 0;
        return new Object[]{true};
    }

    @Callback(doc="function():number, number --get the sequence head and tail")
    public Object[] sequence(Context context, Arguments arguments) throws Exception{
        return new Object[]{sequenceHead, sequenceTail};
    }

    /*
    -------------------------------------------Storage System---------------------------------------------------
     */

    @Override
    public int getSlotNum() {
        return -1;//not applicable
    }

    @Override
    public ItemStack push(int slot, ItemStack itemStack, boolean atomic) {
        return itemStack;//not applicable
    }

    private void tryShiftSequence() {
        while(true){
            List<ItemStack> node = this.storage.get(0);
            if(node.stream().allMatch(ItemStack::isEmpty)){
                this.storage.remove(node);
                this.sequenceHead += node.size();
            } else{
                break;
            }
        }
    }

    @Override
    public ItemStack pop(int slot, int num) {
        int p = sequenceHead;
        for(List<ItemStack> node : this.storage){
            if(p<slot && p+node.size()>=slot){
                int index = slot-p;
                ItemStack stack = node.get(index);
                ItemStack res = stack.split(num);
                tryShiftSequence();
                return res;
            }
            p += node.size();
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public ItemStack check(int slot) {
        int p = sequenceHead;
        for(List<ItemStack> node : this.storage){
            if(p<slot && p+node.size()>=slot){
                int index = slot-p;
                return node.get(index);
            }
            p += node.size();
        }
        return ItemStack.EMPTY;
    }
}
