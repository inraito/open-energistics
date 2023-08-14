package inraito.openerg.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

public class ItemHandlerHelper {
    public static List<ItemStack> pushAll(IInventory srcInventory, IItemHandlerModifiable dstItemHandler){
        List<ItemStack> remaining = new ArrayList<>();
        for(int i=0;i<srcInventory.getContainerSize();i++){
            ItemStack toInsert = srcInventory.getItem(i).copy();
            for(int j=0;j<dstItemHandler.getSlots();j++){
                toInsert = dstItemHandler.insertItem(j, toInsert, false);
                if(toInsert.isEmpty()){
                    break;
                }
            }
            if(!toInsert.isEmpty()){
                remaining.add(toInsert);
            }
        }
        return remaining;
    }

    public static void dropContents(IItemHandler handler, World world, Vector3d pos){
        for(int index=0;index<handler.getSlots();index++){
            ItemStack stack = handler.getStackInSlot(index);
            if(!stack.isEmpty()){
                dropItemStack(world, pos, stack);
            }
        }
    }

    public static void dropItemStack(World world, Vector3d pos, ItemStack itemStack){
        InventoryHelper.dropItemStack(world, pos.x, pos.y, pos.z, itemStack);
    }
}
