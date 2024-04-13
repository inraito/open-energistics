package inraito.openerg.lua.dummy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;

public class TestComponent {
    public static final Globals globals = JsePlatform.standardGlobals();

    @BeforeClass
    public static void init(){

    }

    @Test
    public void testComponent(){
        LuaValue value = globals.loadfile(Lib.parseTest("component.lua"));
        LuaValue res = value.call();
        return;
    }
}
