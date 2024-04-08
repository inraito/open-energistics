package inraito.openerg.common.driver;

import li.cil.oc.api.Driver;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DriverList {
    @SubscribeEvent
    public static void initialize(FMLCommonSetupEvent event){
        Driver.add(new DriverMESwitchingCard());
    }
}
