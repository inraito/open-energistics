package inraito.openerg.common.tileentity;

import inraito.openerg.Lib;
import inraito.openerg.common.block.BlockList;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityList {
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Lib.modid);
    public static DeferredRegister<TileEntityType<?>> getTileEntities(){
        return TileEntityList.TILE_ENTITIES;
    }

    public static final RegistryObject<TileEntityType<OCInterfaceTileEntity>> ocInterfaceTileEntity;
    public static final RegistryObject<TileEntityType<BlockControllerTileEntity>> blockControllerTileEntity;

    static{
        ocInterfaceTileEntity = TILE_ENTITIES.register("oc_interface_tileentity", () ->
                TileEntityType.Builder.of(OCInterfaceTileEntity::new, BlockList.ocInterface.get()).build(null));

        blockControllerTileEntity = TILE_ENTITIES.register("block_controller_tileentity", () ->
                TileEntityType.Builder.of(BlockControllerTileEntity::new, BlockList.blockController.get()).build(null));
    }
}
