package inraito.openerg;

import inraito.openerg.common.block.BlockList;
import inraito.openerg.common.container.ContainerList;
import inraito.openerg.common.item.ItemList;
import inraito.openerg.common.tileentity.TileEntityList;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class RegisterList {
    private static final DeferredRegister<Block> BLOCKS = BlockList.getBlocks();
    private static final DeferredRegister<Item> ITEMS = ItemList.getItems();
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = TileEntityList.getTileEntities();
    private static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = ContainerList.getContainers();

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
        CONTAINER_TYPES.register(eventBus);
    }
}
