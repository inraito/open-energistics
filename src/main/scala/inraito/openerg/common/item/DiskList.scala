package inraito.openerg.common.item

import li.cil.oc.api.{FileSystem, Items}
import net.minecraft.item.{DyeColor, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

import inraito.openerg.util.ResourceLocationUtil.locationOf

object DiskList {
  val machineManagerID = "oemm"
  val oeCoreID = "oecore"

  var oecore:ItemStack = null
  var oemm:ItemStack = null

  def diskLocation(diskID:String):ResourceLocation = locationOf("disk/" + diskID)

  @SubscribeEvent
  def registerDisk(event:FMLCommonSetupEvent):Unit = {
    oemm = Items.registerFloppy(
      machineManagerID,
      locationOf(machineManagerID),
      DyeColor.BLUE,
      ()=>{
        FileSystem.fromResource(diskLocation(machineManagerID))
      },
      false
    )

    oecore = Items.registerFloppy(
      oeCoreID,
      locationOf(oeCoreID),
      DyeColor.WHITE,
      ()=>{
        FileSystem.fromResource(diskLocation(oeCoreID))
      },
      false
    )
  }

}
