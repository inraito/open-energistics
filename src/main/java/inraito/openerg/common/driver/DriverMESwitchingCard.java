package inraito.openerg.common.driver;

import inraito.openerg.common.item.ItemList;
import inraito.openerg.util.nbt.NBT2Collection;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverItem;
import li.cil.oc.common.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static inraito.openerg.common.tileentity.storage.StorageStaticLib.*;

public class DriverMESwitchingCard extends DriverItem {
    public static class MESwitchingCardEnvironment extends AbstractManagedEnvironment{
        private static final int MAX_SIZE = 5;

        public MESwitchingCardEnvironment(){
            super();
            this.setNode(Network.newNode(this, Visibility.Network).
                    withComponent("me_switching_card").create());
        }

        private final Stack<ItemStack> stack = new Stack<>();

        @Override
        public boolean canUpdate() {
            return false;
        }

        @Override
        public void saveData(CompoundNBT nbt) {
            super.saveData(nbt);
            ListNBT list = new ListNBT();
            for(ItemStack item : this.stack){
                CompoundNBT data = new CompoundNBT();
                list.add(item.save(data));
            }
            nbt.put("stack_data", list);
        }

        @Override
        public void loadData(CompoundNBT nbt) {
            super.loadData(nbt);
            ListNBT list = ((ListNBT) nbt.get("stack_data"));
            for(INBT inbt : list){
                CompoundNBT item = ((CompoundNBT) inbt);
                ItemStack itemStack = ItemStack.of(item);
                this.stack.push(itemStack);
            }
        }

        private Message response = null;
        @Override
        public void onMessage(Message message) {
            if(message.name().equals(RESPONSE_NAME)){
                if(this.response!=null){
                    throw new IllegalStateException("Response Override!");
                }
                this.response = message;
            }
            super.onMessage(message);
        }

        @Callback(doc=  "function(addr:string, slot:int, num:int):boolean -- try pushing the given number of items" +
                        " in the give slot of the given storage system into the card, true for stack expands, exact" +
                        " number should be checked via peekTop subsequently")
        public Object[] push(Context context, Arguments arguments) throws Exception{
            if(this.stack.size()>=MAX_SIZE){
                return new Object[]{null, "stack is full"};
            }
            try {
                String address = arguments.checkString(0);
                int slot = arguments.checkInteger(1);
                int num = arguments.checkInteger(2);
                Node node = this.node();
                node.network().sendToAddress(node, address, REQUEST_NAME, POP, slot, num);
                ItemStack popped = ((ItemStack) response.data()[0]).copy();
                this.response = null;
                if(popped.isEmpty()){
                    return new Object[]{false};
                }
                this.stack.push(popped);
                return new Object[]{true};
            }catch (Exception e){
                return new Object[]{false};
            }
        }

        @Callback(doc="function(addr:string, slot:int):boolean -- pop the ItemStack at top to the give slot of the given storage system, all or none")
        public Object[] pop(Context context, Arguments arguments) throws Exception{
            if(this.stack.size()<=0){
                return new Object[]{null, "stack is empty"};
            }
            try {
                String address = arguments.checkString(0);
                int slot = arguments.checkInteger(1);
                Node node = this.node();
                node.network().sendToAddress(node, address, REQUEST_NAME,
                        PUSH, slot, this.stack.peek().copy(), true);
                ItemStack returned = ((ItemStack) this.response.data()[0]).copy();
                this.response = null;
                if(returned.isEmpty()){
                    this.stack.pop();
                    return new Object[]{true};
                }else{
                    return new Object[]{false};
                }
            }catch (Exception e){
                return new Object[]{false};
            }
        }

        @Callback(doc="function():table -- peek the ItemStack on the top")
        public Object[] peekTop(Context context, Arguments arguments) throws Exception{
            if(this.stack.size()<=0){
                return new Object[]{null, "stack is empty"};
            }
            ItemStack top = this.stack.peek();
            return new Object[]{toTable(top)};
        }

        @Callback(doc="function(addr:String, slot:int):table -- peek the ItemStack in a storage system")
        public Object[] peek(Context context, Arguments arguments) throws Exception{
            try {
                String address = arguments.checkString(0);
                int slot = arguments.checkInteger(1);
                Node node = this.node();
                node.network().sendToAddress(node, address, REQUEST_NAME,
                        CHECK, slot);
                ItemStack returned = ((ItemStack) this.response.data()[0]).copy();
                this.response = null;
                if(returned.isEmpty()){
                    return new Object[]{null};
                }
                return new Object[]{toTable(returned)};
            }catch (Exception e){
                return new Object[]{null, "failed for unclassified reasons"};
            }
        }

        private static Map<String, Object> toTable(ItemStack itemStack){
            Map<String, Object> stackNBT = NBT2Collection.toMap(itemStack.getOrCreateTag());
            Map<String, Object> res = new HashMap<>();
            res.put("registry_name", itemStack.getItem().getRegistryName());
            res.put("num", itemStack.getCount());
            res.put("nbt", stackNBT);
            return res;
        }

    }

    public DriverMESwitchingCard(){
        super(new ItemStack(ItemList.meSwitchingCardItem.get()));
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        return new MESwitchingCardEnvironment();
    }

    @Override
    public String slot(ItemStack stack) {
        return Slot.Card();
    }
}
