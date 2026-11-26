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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class LuaDiskTest {
    private static final Path TESTS = Paths.get(System.getProperty("openerg.luaTests", "src/test/lua"));

    private static final List<String> EDITIONS = Arrays.asList("performance", "robust");

    @Parameterized.Parameters(name = "oecore={0}, oemm={1}, {2}")
    public static List<Object[]> scripts() throws Exception {
        List<Path> scripts;
        try (Stream<Path> files = Files.walk(TESTS)) {
            scripts = files.filter(path -> path.toString().endsWith("_test.lua"))
                    .map(TESTS::relativize).sorted().collect(Collectors.toList());
        }
        assertFalse("Expected Lua tests under " + TESTS, scripts.isEmpty());
        List<Object[]> cases = new ArrayList<>();
        for (String core : EDITIONS) {
            for (String manager : EDITIONS) {
                for (Path path : scripts) {
                    String component = path.getName(0).toString();
                    String edition = component.equals("oecore") ? core : manager;
                    String other = edition.equals("performance") ? "robust" : "performance";
                    if (!path.toString().endsWith("_" + other + "_test.lua")) {
                        cases.add(new Object[]{core, manager, path});
                    }
                }
            }
        }
        return cases;
    }

    @Parameterized.Parameter(0) public String core;
    @Parameterized.Parameter(1) public String manager;
    @Parameterized.Parameter(2) public Path script;

    @Test(timeout = 10000)
    public void runLuaScript() throws Exception {
        Globals lua = JsePlatform.standardGlobals();
        lua.finder = name -> {
            for (String disk : new String[]{"oecore", "oemm"}) {
                InputStream resource = getClass().getResourceAsStream("/assets/openerg/disk/" + disk + "/"
                        + (disk.equals("oecore") ? core : manager) + name);
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
