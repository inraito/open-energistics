package inraito.openerg.common.item

import org.junit.Assert._
import org.junit.Test
import li.cil.oc.api.API
import li.cil.oc.api.detail.{FileSystemAPI, ItemAPI}
import li.cil.oc.api.fs.FileSystem
import net.minecraft.item.DyeColor
import net.minecraft.util.ResourceLocation
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.ArgumentCaptor
import java.util.concurrent.Callable

class DiskListTest {
  @Test def registersBothFloppiesWithLoadableFactories(): Unit = {
    val oldItems = API.items
    val oldFs = API.fileSystem
    val oldCore = DiskList.oecore
    val oldManager = DiskList.oemm
    try {
      API.items = mock(classOf[ItemAPI])
      API.fileSystem = mock(classOf[FileSystemAPI])
      DiskList.registerDisk(null)
      for ((id, color) <- Seq(("oecore", DyeColor.WHITE), ("oemm", DyeColor.BLUE))) {
        val factory = ArgumentCaptor.forClass(classOf[Callable[FileSystem]])
        verify(API.items).registerFloppy(eqTo(id), eqTo(new ResourceLocation("openerg", id)),
          eqTo(color), factory.capture(), eqTo(false))
        factory.getValue.call()
        verify(API.fileSystem).fromResource(new ResourceLocation("openerg", "disk/" + id))
      }
    } finally {
      API.items = oldItems
      API.fileSystem = oldFs
      DiskList.oecore = oldCore
      DiskList.oemm = oldManager
    }
  }

  @Test def registeredDiskPathsContainTheirLuaEntrypoints(): Unit = {
    val disks = Seq(
      (DiskList.oeCoreID, "openerg:disk/oecore", "lib/oe.lua", "etc/oecore/init.lua"),
      (DiskList.machineManagerID, "openerg:disk/oemm", "lib/oemm.lua", "etc/oemm/mm1/init.lua")
    )
    for ((id, expected, library, init) <- disks) {
      val location = DiskList.diskLocation(id)
      assertEquals(expected, location.toString)
      for (file <- Seq(library, init)) {
        val path = s"/assets/${location.getNamespace}/${location.getPath}/$file"
        assertNotNull(path, getClass.getResource(path))
      }
    }
  }
}
