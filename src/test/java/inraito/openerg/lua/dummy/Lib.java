package inraito.openerg.lua.dummy;

public class Lib {
    public static final String SRC_PATH = "./src/resources/";//TODO
    public static final String TEST_PATH = "./src/test/lua";

    public static String parseTest(String relative){
        return TEST_PATH + "/" + relative;
    }
}
