package inraito.openerg.common.driver;

import inraito.openerg.Lib;
import li.cil.oc.api.Driver;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Lib.modid, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DriverList {
    public static final OCInterfaceDriver ocInterface = new OCInterfaceDriver();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event){
        Driver.add(ocInterface);
    }
}
