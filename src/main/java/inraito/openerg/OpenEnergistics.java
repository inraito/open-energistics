package inraito.openerg;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Lib.modid)
public class OpenEnergistics {
    public OpenEnergistics() {
        RegisterList.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
