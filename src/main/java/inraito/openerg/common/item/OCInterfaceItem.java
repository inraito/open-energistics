package inraito.openerg.common.item;

import inraito.openerg.common.block.BlockList;
import inraito.openerg.common.item.group.ModGroup;
import net.minecraft.item.BlockItem;

public class OCInterfaceItem extends BlockItem {
    public OCInterfaceItem() {
        super(BlockList.ocInterface.get(), new Properties().tab(ModGroup.openEnergisticsGroup));
    }
}
