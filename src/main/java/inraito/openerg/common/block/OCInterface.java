package inraito.openerg.common.block;

import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class OCInterface extends Block {
    public OCInterface() {
        super(Properties.of(Material.METAL));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OCInterfaceTileEntity();
    }
}
