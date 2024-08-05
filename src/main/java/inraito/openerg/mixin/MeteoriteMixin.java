package inraito.openerg.mixin;

import appeng.util.InventoryAdaptor;
import appeng.worldgen.meteorite.MeteoritePlacer;
import inraito.openerg.common.item.DiskList;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * This mixin is used to make OE disks spawned in AE meteorite chests.
 */
@Mixin(value = MeteoritePlacer.class)
public class MeteoriteMixin {
    @Inject(method = "placeChest",
            at = @At(value = "CONSTANT", args = "intValue=3", ordinal = 0, by = 2),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false)
    protected void addOEDisks(CallbackInfo ci, TileEntity te, InventoryAdaptor ap, int primary){
        double r = Math.random();
        if(r < 0.5){
            if(r < 0.25) {
                ap.addItems(DiskList.oemm().copy());
            }else{
                ap.addItems(DiskList.oecore().copy());
            }
        }
    }
}
