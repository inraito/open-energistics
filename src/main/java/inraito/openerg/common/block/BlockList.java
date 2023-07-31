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

    public static final RegistryObject<OCInterface> ocInterface;

    static{
        ocInterface = BLOCKS.register("oc_interface", OCInterface::new);
    }


}
