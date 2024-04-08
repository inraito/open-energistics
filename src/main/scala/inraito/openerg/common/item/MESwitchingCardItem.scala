package inraito.openerg.common.item

import inraito.openerg.common.item.group.ModGroup
import li.cil.oc.common.item.traits.SimpleItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Properties
import net.minecraftforge.common.extensions.IForgeItem

class MESwitchingCardItem extends Item(new Properties().tab(ModGroup.openEnergisticsGroup)) with SimpleItem with IForgeItem{
  override def getDescriptionId(): String = super.getOrCreateDescriptionId();

}
