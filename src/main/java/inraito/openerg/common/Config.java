package inraito.openerg.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec CONFIG;
    public static ForgeConfigSpec.IntValue MAXIMUM_PATTERNS;

    static {
        ForgeConfigSpec.Builder Builder = new ForgeConfigSpec.Builder();
        Builder.push("general");
        MAXIMUM_PATTERNS = Builder.comment("Max patterns allowed in a single OCInterface")
                .defineInRange("maximum_patterns", 64, 1, 1024);
        Builder.pop();
        CONFIG = Builder.build();
    }
}
