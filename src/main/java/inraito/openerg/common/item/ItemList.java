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
    public static final RegistryObject<OEInterfaceItem> oeInterfaceItem;
    public static final RegistryObject<CallbackMarker> callbackMarker;
    public static final RegistryObject<BlockControllerItem> blockControllerItem;
    public static final RegistryObject<MESwitchingCardItem> meSwitchingCardItem;

    static{
        ocInterfaceItem = ITEMS.register("oc_interface", OCInterfaceItem::new);
        oeInterfaceItem = ITEMS.register("oe_interface", OEInterfaceItem::new);
        callbackMarker = ITEMS.register("callback_marker", CallbackMarker::new);
        blockControllerItem = ITEMS.register("block_controller", BlockControllerItem::new);
        meSwitchingCardItem = ITEMS.register("me_switching_card", MESwitchingCardItem::new);
    }
}
