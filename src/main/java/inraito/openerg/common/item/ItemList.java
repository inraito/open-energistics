package inraito.openerg.common.item;

import inraito.openerg.Lib;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemList {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Lib.modid);
    public static DeferredRegister<Item> getItems(){
        return ItemList.ITEMS;
    }

    public static final RegistryObject<OCInterfaceItem> ocInterfaceItem;

    static{
        ocInterfaceItem = ITEMS.register("oc_interface", OCInterfaceItem::new);
    }
}
