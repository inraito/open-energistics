package inraito.openerg.common.item;

import inraito.openerg.common.block.BlockList;
import inraito.openerg.common.item.group.ModGroup;
import net.minecraft.item.BlockItem;

public class OEInterfaceItem extends BlockItem {
    public OEInterfaceItem() {
        super(BlockList.oeInterface.get(), new Properties().tab(ModGroup.openEnergisticsGroup));
    }
}
