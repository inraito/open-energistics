package inraito.openerg.common.item.group;

import inraito.openerg.common.item.ItemList;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class OpenEnergisticsGroup extends ItemGroup {
    public OpenEnergisticsGroup() {
        super("open_energistics");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ItemList.ocInterfaceItem.get());
    }
}
