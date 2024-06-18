package inraito.openerg.common.item

import li.cil.oc.api.{FileSystem, Items}
import net.minecraft.item.{DyeColor, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

import inraito.openerg.util.ResourceLocationUtil.locationOf

object DiskList {
  val machineManagerID = "oemm"

  def diskLocation(diskID:String):ResourceLocation = locationOf("disk/" + diskID)

  @SubscribeEvent
  def registerDisk(event:FMLCommonSetupEvent):Unit = {
    Items.registerFloppy(
      machineManagerID,
      locationOf(machineManagerID),
      DyeColor.BLUE,
      ()=>{
        FileSystem.fromResource(diskLocation(machineManagerID))
      },
      false
    )
  }

}
