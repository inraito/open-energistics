package inraito.openerg.common.item;

import inraito.openerg.common.item.group.ModGroup;
import net.minecraft.item.Item;

class MESwitchingCardItem extends Item{
    public MESwitchingCardItem() {
        super(new Properties().tab(ModGroup.openEnergisticsGroup));
    }
}
