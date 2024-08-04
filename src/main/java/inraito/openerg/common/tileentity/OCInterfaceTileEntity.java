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
import inraito.openerg.Lib;
import inraito.openerg.common.Config;
import inraito.openerg.common.container.OCInterfaceContainer;
import inraito.openerg.common.item.ItemList;
import inraito.openerg.util.ItemHandlerHelper;
import li.cil.oc.api.API;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Packet;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class OCInterfaceTileEntity extends TileEntityEnvironment implements IGridHost,
        IGridBlock, ICraftingProvider, INamedContainerProvider, ITickableTileEntity {
    private static final String CONFIG_INVENTORY_KEY = "config_inventory";
    private static final String STORAGE_INVENTORY_KEY = "storage_inventory";
    private static final String BROADCAST_PORT_KEY = "broadcast_port";

    //slot used to config this interface, i.e. using ae crafting patterns to add more recipes
    public ItemStackHandler configInventory = new ItemStackHandler(1){
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            //only encoded patterns are accepted
            return Api.instance().crafting().isEncodedPattern(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            OCInterfaceTileEntity.this.setChanged();
        }
    };
    //slots used to cache output(i/o all from the perspective of an ae network)
    //and input should not be send here, which is not similar to an me interface
    public final ItemStackHandler storageInventory = new ItemStackHandler(27);
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
        nbt.putInt(BROADCAST_PORT_KEY, this.port);
        nbt.put(CONFIG_INVENTORY_KEY, configInventory.serializeNBT());
        nbt.put(STORAGE_INVENTORY_KEY, storageInventory.serializeNBT());
        saveCraftingPatterns(nbt);
        savePending(nbt);
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        this.port = nbt.getInt(BROADCAST_PORT_KEY);
        configInventory.deserializeNBT(nbt.getCompound(CONFIG_INVENTORY_KEY));
        storageInventory.deserializeNBT(nbt.getCompound(STORAGE_INVENTORY_KEY));
        loadCraftingPatterns(nbt);
        loadPending(nbt);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
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

    public void onRemove(){
        this.getGridNode(AEPartLocation.INTERNAL).destroy();
        //drop contents
        BlockPos blockPos = this.getBlockPos();
        Vector3d pos = new Vector3d(blockPos.getX()+0.5, blockPos.getY()+0.5, blockPos.getZ()+0.5);
        ItemHandlerHelper.dropContents(this.configInventory, this.level, pos);
        ItemHandlerHelper.dropContents(this.storageInventory, this.level, pos);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(node!=null){
            node.remove();
        }
    }

    @Override
    public void onLoad() {

    }

    /*
    -----------------------------------------------------Tick-------------------------------------------------------
     */

    private boolean nodeUpdated = false;
    private int count = 0;
    @Override
    public void tick() {
        if (node != null && node.network() == null) {
            API.network.joinOrCreateNetwork(this);
        }
        count = (count+1)%20;
        if(!this.level.isClientSide && count==0 && !this.pendingContext.isEmpty()){
            List<ItemStack> pending = new ArrayList<>();
            for(ItemStack stack : this.waitingToSend){
                ItemStack s = stack.copy();
                for(int i=0;i<this.storageInventory.getSlots();i++){
                    s = this.storageInventory.insertItem(i, s, false);
                    if(stack.isEmpty()){
                        break;
                    }
                }
                if(!s.isEmpty()){
                    pending.add(s);
                }
            }
            if(pending.isEmpty()){
                this.broadcastMessage(this.pendingContext);
                this.pendingContext = new CraftingContext();
                this.waitingToSend.clear();
            }else{
                this.waitingToSend.clear();
                this.waitingToSend.addAll(pending);
            }
        }
        //initialize the ae node, this is here because it seems i can't do that in onLoad()
        if(!nodeUpdated){
            this.getGridNode(AEPartLocation.INTERNAL);
            nodeUpdated = true;
        }
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
        return new OCInterfaceContainer(id, this,player);
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
    private void saveCraftingPatterns(CompoundNBT nbt){
        ListNBT list = new ListNBT();
        for(ItemStack stack : craftingPatterns.keySet()){
            CompoundNBT entry = new CompoundNBT();
            CompoundNBT stackCompound = new CompoundNBT();
            stack.save(stackCompound);
            entry.put("stack", stackCompound);
            entry.put("context", craftingPatterns.get(stack).serializeNBT());
            list.add(entry);
        }
        nbt.put("crafting_patterns", list);
    }

    private void loadCraftingPatterns(CompoundNBT nbt){
        ListNBT list = ((ListNBT) nbt.get("crafting_patterns"));
        for(int i=0;i<list.size();i++){
            CompoundNBT entry = list.getCompound(i);
            ItemStack stack = ItemStack.of(entry.getCompound("stack"));
            CraftingContext context = new CraftingContext();
            context.deserializeNBT(entry.getCompound("context"));
            this.craftingPatterns.put(stack, context);
        }
    }

    private final Map<ItemStack, CraftingContext> craftingPatterns = new HashMap<>();
    public static class CraftingContext implements INBTSerializable<CompoundNBT> {
        String message;
        public CraftingContext(){}

        public CraftingContext(String message){
            this.message = message;
            this.empty = false;
        }

        private boolean empty = true;
        public boolean isEmpty(){
            return this.empty;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT res = new CompoundNBT();
            res.putBoolean("empty", empty);
            if(empty){
                return res;
            }
            res.putString("message", this.message);
            return res;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.empty = nbt.getBoolean("empty");
            if(!this.empty){
                this.message = nbt.getString("message");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CraftingContext that = (CraftingContext) o;
            if(this.empty&&that.empty) return true;
            return Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message, empty);
        }
    }

    private int port = 2048;
    private void broadcastMessage(CraftingContext context){
        Packet packet = Network.newPacket(this.node.address(), null, port, new Object[]{context.message});
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

    private final List<ItemStack> waitingToSend = new ArrayList<>();
    //this may not be '==' to any contexts in *craftingPatterns*.
    private CraftingContext pendingContext = new CraftingContext();
    private void savePending(CompoundNBT nbt){
        ListNBT waitingStacks = new ListNBT();
        for(ItemStack stack:waitingToSend){
            CompoundNBT stackCompound = new CompoundNBT();
            stack.save(stackCompound);
            waitingStacks.add(stackCompound);
        }
        nbt.put("waiting_stacks", waitingStacks);
        nbt.put("pending_context", pendingContext.serializeNBT());
    }

    private void loadPending(CompoundNBT nbt){
        ListNBT waitingStacks = ((ListNBT) nbt.get("waiting_stacks"));
        for(int i=0;i<waitingStacks.size();i++){
            ItemStack stack = ItemStack.of(waitingStacks.getCompound(i));
            this.waitingToSend.add(stack);
        }
        this.pendingContext = new CraftingContext();
        this.pendingContext.deserializeNBT(nbt.getCompound("pending_context"));
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, CraftingInventory table) {
        if(!cachedDetails.containsKey(patternDetails) || !waitingToSend.isEmpty()){
            return false;
        }
        List<ItemStack> remaining = ItemHandlerHelper.pushAll(table, this.storageInventory);
        CraftingContext context = this.craftingPatterns.get(this.cachedDetails.get(patternDetails));
        if(!remaining.isEmpty()){
            this.waitingToSend.addAll(remaining);
            this.pendingContext = context;
            return true;
        }
        this.broadcastMessage(context);
        return true;
    }

    @Override
    public boolean isBusy() {
        return !this.waitingToSend.isEmpty();
    }

    /*
    -------------------------------------OpenComputers Component Callbacks------------------------------------------
     */

    @Callback(doc="function(message:string):boolean --register the pattern in the config slot with the given message, return true if succeed")
    public Object[] registerPattern(Context context, Arguments arguments) throws Exception{
        String message = arguments.checkString(0);
        if(message.isEmpty()){
            throw new IllegalArgumentException();
        }
        ItemStack pattern = this.configInventory.getStackInSlot(0);
        if(pattern.isEmpty()){
            return new Object[]{false, "config slot empty"};
        }
        if(this.craftingPatterns.values().stream()
                .map((ctx)->ctx.message).collect(Collectors.toSet()).contains(message)){
            return new Object[]{false, "message has been used"};
        }
        if(craftingPatterns.keySet().size()>=Config.MAXIMUM_PATTERNS.get()){
            return new Object[]{false, "pattern number reach limit"};
        }
        if(Api.instance().crafting().decodePattern(pattern, this.level)==null){
            return new Object[]{false, "pattern not valid"};
        }

        CraftingContext craftingContext = new CraftingContext(message);
        this.craftingPatterns.put(pattern.copy(), craftingContext);
        this.aeNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.aeNode));
        this.setChanged();
        return new Object[]{true};
    }

    @Callback(doc="function():table --list all registered patterns, only the registered messages are returned")
    public Object[] listAllPatterns(Context context, Arguments arguments) throws Exception{
        List<String> res = new ArrayList<>();
        for(CraftingContext craftingContext : craftingPatterns.values()){
            res.add(craftingContext.message);
        }
        return new Object[]{res};
    }

    @Callback(doc="function(message:string):table, table --get pattern details, output first, input follows")
    public Object[] getPattern(Context context, Arguments arguments) throws Exception{
        String msg = arguments.checkString(0);
        Optional<Map.Entry<ItemStack, CraftingContext>> entry = craftingPatterns.entrySet()
                .stream().filter((e)->e.getValue().message.equals(msg))
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
        String msg = arguments.checkString(0);
        Optional<ItemStack> optional = this.craftingPatterns.keySet().stream().filter(k->craftingPatterns.get(k)
                .message.equals(msg)).findFirst();
        if(!optional.isPresent()){
            return new Object[]{false, "message not registered"};
        }
        this.craftingPatterns.remove(optional.get());
        this.aeNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.aeNode));
        this.setChanged();
        return new Object[]{true};
    }
}
