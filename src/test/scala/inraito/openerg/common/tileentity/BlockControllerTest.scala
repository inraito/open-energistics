package inraito.openerg.common.tileentity

import li.cil.oc.api.fs.FileSystem
import li.cil.oc.api.fs.Mode
import inraito.openerg.util.IndexMapOnFS
import inraito.openerg.HeadlessItems
import net.minecraft.item.{Items, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandler, ItemStackHandler}
import li.cil.oc.api.machine.Arguments
import li.cil.oc.server.fs.VirtualFileSystem
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito._

class BlockControllerTest extends HeadlessItems {
  private def controller(disk: FileSystem): BlockControllerTileEntity = {
    // Bypass the constructor's Forge registry lookup and OC node creation.
    val tile = mock(classOf[BlockControllerTileEntity], CALLS_REAL_METHODS)
    doReturn(disk, Array.empty[Object]: _*).when(tile).fs()
    doNothing().when(tile).setChanged()
    tile
  }

  private def newDisk(): FileSystem =
    new VirtualFileSystem {
      override def spaceTotal(): Long = 65536L
      override def spaceUsed(): Long = 0L
    }
  private def mapping(index: Int): Arguments = {
    val args = mock(classOf[Arguments])
    when(args.checkInteger(0)).thenReturn(index)
    when(args.checkString(1)).thenReturn("up")
    when(args.checkString(2)).thenReturn("down")
    when(args.checkInteger(3)).thenReturn(4)
    args
  }

  private def withAdjacentInventory(count: Int): (BlockControllerTileEntity, ItemStackHandler) = {
    val tile = controller(newDisk())
    val world = mock(classOf[World])
    val neighbor = mock(classOf[TileEntity])
    val inventory = new ItemStackHandler(5)
    inventory.setStackInSlot(4, new ItemStack(Items.COAL, count))
    val capability = LazyOptional.of[IItemHandler](() => inventory)
    when(neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN)).thenReturn(capability)
    when(world.getBlockEntity(BlockPos.ZERO.above())).thenReturn(neighbor)
    tile.setLevelAndPosition(world, BlockPos.ZERO)
    assertEquals(true, tile.map(null, mapping(12))(0))
    (tile, inventory)
  }

  @Test def atomicInsertionRejectsOverflowAndPartialInsertionReturnsRemainder(): Unit = {
    val (tile, inventory) = withAdjacentInventory(63)
    val input = new ItemStack(Items.COAL, 2)
    assertEquals(2, tile.push(12, input, true).getCount)
    assertEquals(63, inventory.getStackInSlot(4).getCount)
    assertEquals(1, tile.push(12, input, false).getCount)
    assertEquals(64, inventory.getStackInSlot(4).getCount)
    assertEquals(2, input.getCount)
  }

  @Test def checkReturnsIndependentSnapshotAndPopRemovesOnlyRequestedCount(): Unit = {
    val (tile, inventory) = withAdjacentInventory(6)
    tile.check(12).shrink(1)
    assertEquals(6, inventory.getStackInSlot(4).getCount)
    assertEquals(2, tile.pop(12, 2).getCount)
    assertEquals(4, tile.check(12).getCount)
  }

  @Test def minusOneExtractsEntireMappedSlot(): Unit = {
    val (tile, inventory) = withAdjacentInventory(3)
    val extracted = tile.pop(12, -1)
    assertEquals("remaining=" + inventory.getStackInSlot(4).getCount, 3, extracted.getCount)
    assertTrue(inventory.getStackInSlot(4).isEmpty)
  }

  @Test def failedDiskWriteIsReported(): Unit = {
    val disk = newDisk()
    disk.makeDirectory("/" + IndexMapOnFS.DATA_FOLDER)
    disk.makeDirectory(IndexMapOnFS.parsePath(2)) // A directory occupies the intended file.
    assertEquals(false, controller(disk).map(null, mapping(2))(0))
  }

  @Test def removingUnknownIndexDoesNotCreateDirectories(): Unit = {
    val disk = newDisk()
    IndexMapOnFS.remove(disk, 1234)
    assertFalse(disk.exists("/" + IndexMapOnFS.DATA_FOLDER))
  }

  @Test def clearKeepsUnrelatedFilesAndRemovesMappingData(): Unit = {
    val disk = newDisk()
    disk.getHandle(disk.open("/unrelated", Mode.Write)).close()
    val tile = controller(disk)
    tile.map(null, mapping(1))
    tile.map(null, mapping(12))
    IndexMapOnFS.clear(disk)
    assertFalse(IndexMapOnFS.containsKey(disk, 1))
    assertFalse(IndexMapOnFS.containsKey(disk, 12))
    assertTrue(disk.exists("/unrelated"))
    assertFalse("mapping directories still consume disk space", disk.exists("/" + IndexMapOnFS.DATA_FOLDER))
  }

  @Test def mappingSurvivesControllerReplacementAndCanBeRemoved(): Unit = {
    val disk = newDisk()
    val writer = controller(disk)
    val args = mapping(12)

    assertEquals(true, writer.map(null, args)(0))
    when(args.checkInteger(3)).thenReturn(9)
    assertEquals(false, writer.map(null, args)(0))

    val reader = controller(disk)
    val entry = reader.check(null, args)(0).asInstanceOf[java.util.Map[_, _]]
    assertEquals("up", entry.get("relative"))
    assertEquals("down", entry.get("side"))
    assertEquals(4, entry.get("slot"))
    assertEquals(true, reader.unmap(null, args)(0))
    assertNull(writer.check(null, args)(0))
    assertEquals(false, reader.unmap(null, args)(0))
  }
}
