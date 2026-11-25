package inraito.openerg;

import org.junit.BeforeClass;

// Initialize vanilla registries in their normal order, before Item or ItemStack loads.
// No server, world, mod lifecycle or render thread is created.
public abstract class HeadlessItems {
    @BeforeClass
    public static void initializeRegistries() throws ClassNotFoundException {
        Class.forName("net.minecraft.util.registry.Registry");
    }
}
