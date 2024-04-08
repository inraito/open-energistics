package inraito.openerg

import inraito.openerg.common.Config
import inraito.openerg.common.container.ContainerList
import inraito.openerg.common.driver.DriverList
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.{ModContainer, ModLoadingContext}
import net.minecraftforge.scorge.lang.ScorgeModLoadingContext

class OpenEnergistics {
  val modContainer: ModContainer = ModLoadingContext.get.getActiveContainer
  private val eventbus : IEventBus = ScorgeModLoadingContext.get.getModEventBus
  RegisterList.register(eventbus)
  eventbus.register(classOf[ContainerList])
  eventbus.register(classOf[DriverList])
  ModLoadingContext.get.registerConfig(ModConfig.Type.COMMON, Config.CONFIG)

}
