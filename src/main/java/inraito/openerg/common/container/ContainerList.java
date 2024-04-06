package inraito.openerg.common.container;

import inraito.openerg.Lib;
import inraito.openerg.client.screen.OCInterfaceScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerList {
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Lib.modid);
    public static DeferredRegister<ContainerType<?>> getContainers(){
        return CONTAINERS;
    }

    public static final RegistryObject<ContainerType<OCInterfaceContainer>> ocInterfaceContainer;

    static{
        ocInterfaceContainer = CONTAINERS.register("oc_interface", ()->
                IForgeContainerType.create((windowId, inv, data) ->
                        new OCInterfaceContainer(windowId)));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            ScreenManager.register(ContainerList.ocInterfaceContainer.get(),
                    OCInterfaceScreen::new);
        });
    }

}
