package inraito.openerg.util;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerHelper {
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
