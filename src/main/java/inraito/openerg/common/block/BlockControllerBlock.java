package inraito.openerg.common.block;

import inraito.openerg.common.tileentity.BlockControllerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class BlockControllerBlock extends Block {
    public BlockControllerBlock() {
        super(Properties.of(Material.METAL).harvestLevel(1)
                .harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel((state)-> 15));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockControllerTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
