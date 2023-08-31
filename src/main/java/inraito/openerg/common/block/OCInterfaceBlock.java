package inraito.openerg.common.block;

import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class OCInterfaceBlock extends Block {
    public OCInterfaceBlock() {
        super(Properties.of(Material.METAL).harvestLevel(1)
                .harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel((state)-> 15));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OCInterfaceTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        if(!pLevel.isClientSide && pHand==Hand.MAIN_HAND){
            OCInterfaceTileEntity tileEntity = ((OCInterfaceTileEntity) pLevel.getBlockEntity(pPos));
            NetworkHooks.openGui(((ServerPlayerEntity) pPlayer), tileEntity, (packetBuffer)->{});
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        OCInterfaceTileEntity tileEntity = ((OCInterfaceTileEntity) pLevel.getBlockEntity(pPos));
        tileEntity.onRemove();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
