package inraito.openerg.lua;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LuaPerformanceTest {
    @Test
    public void installedSourcesOmitEveryOptionalCheck() throws Exception {
        Path root = Paths.get(System.getProperty("openerg.disks", "src/main/resources/assets/openerg/disk"));
        for (String disk : new String[]{"oecore", "oemm"}) {
            List<Path> scripts;
            Path sourceDir = root.resolve(disk);
            try (Stream<Path> paths = Files.walk(sourceDir)) {
                scripts = paths.filter(path -> path.toString().endsWith(".lua")).collect(Collectors.toList());
            }
            assertFalse(disk, scripts.isEmpty());
            for (Path script : scripts) {
                String path = sourceDir.relativize(script).toString().replace('\\', '/');
                String resource = "/assets/openerg/disk/" + disk + "/performance/" + path;
                try (InputStream input = getClass().getResourceAsStream(resource)) {
                    assertNotNull(resource, input);
                    List<String> expected = Files.readAllLines(script, StandardCharsets.UTF_8);
                    expected.replaceAll(line -> line.trim().startsWith("--@check ") ? "" : line);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                    assertEquals(resource, expected, reader.lines().collect(Collectors.toList()));
                }
            }
        }
    }
}
