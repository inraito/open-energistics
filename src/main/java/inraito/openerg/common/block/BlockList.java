package inraito.openerg.common.block;

import inraito.openerg.Lib;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockList {
    private static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Lib.modid);
    public static DeferredRegister<Block> getBlocks(){
        return BlockList.BLOCKS;
    }

    public static final RegistryObject<OCInterfaceBlock> ocInterface;
    public static final RegistryObject<OEInterfaceBlock> oeInterface;
    public static final RegistryObject<BlockControllerBlock> blockController;

    static{
        ocInterface = BLOCKS.register("oc_interface", OCInterfaceBlock::new);
        oeInterface = BLOCKS.register("oe_interface", OEInterfaceBlock::new);
        blockController = BLOCKS.register("block_controller", BlockControllerBlock::new);
    }


}
