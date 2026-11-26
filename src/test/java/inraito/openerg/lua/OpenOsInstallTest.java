package inraito.openerg.lua;

import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

public class OpenOsInstallTest {
    @Test
    public void stockInstallerSelectsEachEditionAndPreservesConfiguration() throws Exception {
        Path tests = Paths.get(System.getProperty("openerg.luaTests", "src/test/lua"));
        for (String disk : new String[]{"oecore", "oemm"}) {
            Globals lua = JsePlatform.standardGlobals();
            String props = "/assets/openerg/disk/" + disk + "/.prop";
            try (InputStream metadata = getClass().getResourceAsStream(props);
                 InputStream installer = getClass().getResourceAsStream(
                         "/assets/opencomputers/loot/openos/lib/core/install_basics.lua");
                 InputStream fixture = Files.newInputStream(tests.resolve("openos_install.lua"))) {
                assertNotNull(props, metadata);
                assertNotNull("OpenOS install_basics.lua", installer);
                BufferedReader reader = new BufferedReader(new InputStreamReader(metadata, StandardCharsets.UTF_8));
                lua.set("properties", reader.lines().collect(Collectors.joining("\n")));
                lua.set("installer", lua.load(installer, "@install_basics.lua", "t", lua));
                lua.load(fixture, "@openos_install.lua", "t", lua).call(LuaValue.valueOf(disk));
            }
        }
    }
}
