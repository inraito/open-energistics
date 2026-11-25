package inraito.openerg.lua;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class LuaDiskTest {
    private static final Path TESTS = Paths.get(System.getProperty("openerg.luaTests", "src/test/lua"));

    @Parameterized.Parameters(name = "{0}")
    public static List<String> scripts() throws Exception {
        try (Stream<Path> files = Files.walk(TESTS)) {
            List<String> scripts = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("_test.lua"))
                    .map(TESTS::relativize).map(Path::toString).sorted().collect(Collectors.toList());
            assertFalse("Expected at least one *_test.lua under " + TESTS, scripts.isEmpty());
            return scripts;
        }
    }

    @Parameterized.Parameter
    public String script;

    @Test(timeout = 10000)
    public void runLuaScript() throws Exception {
        Globals lua = JsePlatform.standardGlobals();
        lua.finder = name -> {
            for (String disk : new String[]{"oecore", "oemm"}) {
                InputStream resource = getClass().getResourceAsStream("/assets/openerg/disk/" + disk + name);
                if (resource != null) return resource;
            }
            return null;
        };
        lua.get("package").set("path", "/lib/?.lua");
        run(lua, TESTS.resolve("opencomputers.lua"));
        run(lua, TESTS.resolve(script));
    }

    private static void run(Globals lua, Path path) throws Exception {
        try (InputStream input = Files.newInputStream(path)) {
            lua.load(input, "@" + path, "t", lua).call();
        }
    }
}
