package inraito.openerg.util;

import inraito.openerg.Lib;
import net.minecraft.util.ResourceLocation;

public class ResourceLocationUtil {
    public static ResourceLocation locationOf(String location){
        return new ResourceLocation(Lib.modid, location);
    }
}
