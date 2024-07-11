package inraito.openerg.common.block;

import inraito.openerg.common.tileentity.OEInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class OEInterfaceBlock extends Block {
    public static final BooleanProperty EMPTY = BooleanProperty.create("empty");
    public OEInterfaceBlock() {
        super(Properties.of(Material.METAL).harvestLevel(1)
                .harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel((state)-> 15));
        this.registerDefaultState(this.stateDefinition.any().setValue(EMPTY, Boolean.TRUE));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OEInterfaceTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        if(!pLevel.isClientSide && pHand==Hand.MAIN_HAND){
            OEInterfaceTileEntity tileEntity = ((OEInterfaceTileEntity) pLevel.getBlockEntity(pPos));
            ItemStack stack = tileEntity.configInventory.getStackInSlot(0).copy();
            if (!stack.isEmpty()){
                ItemHandlerHelper.giveItemToPlayer(pPlayer,tileEntity.configInventory.extractItem(0,1,false));
                if (tileEntity.configInventory.getStackInSlot(0).isEmpty())
                    pLevel.setBlock(pPos,pState.setValue(EMPTY,true),2);
            }else {
                pPlayer.setItemInHand(pHand,tileEntity.configInventory.insertItem(0,pPlayer.getItemInHand(pHand),false));
                if (!tileEntity.configInventory.getStackInSlot(0).isEmpty())
                    pLevel.setBlock(pPos,pState.setValue(EMPTY, false),2);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        OEInterfaceTileEntity tileEntity = ((OEInterfaceTileEntity) pLevel.getBlockEntity(pPos));
        tileEntity.onRemove(pState);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EMPTY);
    }
}
