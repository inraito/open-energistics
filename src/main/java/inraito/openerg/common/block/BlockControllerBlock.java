package inraito.openerg.common.block;

import appeng.util.helpers.ItemHandlerUtil;
import inraito.openerg.common.tileentity.BlockControllerTileEntity;
import inraito.openerg.common.tileentity.OCInterfaceTileEntity;
import li.cil.oc.OpenComputers;
import li.cil.oc.integration.opencomputers.Item;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

    @Override
    public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
        if (!pLevel.isClientSide && pHand == Hand.MAIN_HAND) {
            BlockControllerTileEntity tileEntity = (BlockControllerTileEntity) pLevel.getBlockEntity(pPos);
            ItemStack stack = tileEntity.fsSlot.getStackInSlot(0).copy();
            if (!stack.isEmpty()){
                ItemHandlerHelper.giveItemToPlayer(pPlayer,tileEntity.fsSlot.extractItem(0,1,false));
            }else {
                pPlayer.setItemInHand(pHand,tileEntity.fsSlot.insertItem(0,pPlayer.getItemInHand(pHand),false));
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        BlockControllerTileEntity tileEntity = (BlockControllerTileEntity) pLevel.getBlockEntity(pPos);
        tileEntity.onRemove();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
