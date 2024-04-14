package inraito.openerg.common.block;

import appeng.util.helpers.ItemHandlerUtil;
import inraito.openerg.common.tileentity.BlockControllerTileEntity;
import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import li.cil.oc.OpenComputers;
import li.cil.oc.common.Sound;
import li.cil.oc.integration.opencomputers.Item;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

public class BlockControllerBlock extends Block {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public BlockControllerBlock() {
        super(Properties.of(Material.METAL).harvestLevel(1)
                .harvestTool(ToolType.PICKAXE).strength(3.5F).lightLevel((state)-> 15));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED,Boolean.valueOf(false)));
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

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        if (!pLevel.isClientSide && pHand == Hand.MAIN_HAND) {
            BlockControllerTileEntity tileEntity = (BlockControllerTileEntity) pLevel.getBlockEntity(pPos);
            ItemStack stack = tileEntity.fsSlot.getStackInSlot(0).copy();
            if (!stack.isEmpty()){
                ItemHandlerHelper.giveItemToPlayer(pPlayer,tileEntity.fsSlot.extractItem(0,1,false));
                if (tileEntity.fsSlot.getStackInSlot(0).isEmpty())
                    pLevel.setBlock(pPos,pState.setValue(POWERED,false),2);
            }else {
                pPlayer.setItemInHand(pHand,tileEntity.fsSlot.insertItem(0,pPlayer.getItemInHand(pHand),false));
                if (!tileEntity.fsSlot.getStackInSlot(0).isEmpty())
                    pLevel.setBlock(pPos,pState.setValue(POWERED, true),2);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        BlockControllerTileEntity tileEntity = (BlockControllerTileEntity) pLevel.getBlockEntity(pPos);
        tileEntity.onRemove(pState);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
