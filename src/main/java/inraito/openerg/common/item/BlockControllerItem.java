package inraito.openerg.common.item;

import inraito.openerg.common.block.BlockList;
import inraito.openerg.common.item.group.ModGroup;
import net.minecraft.item.BlockItem;

public class BlockControllerItem extends BlockItem {
    public BlockControllerItem() {
        super(BlockList.blockController.get(), new Properties().tab(ModGroup.openEnergisticsGroup));
    }
}
