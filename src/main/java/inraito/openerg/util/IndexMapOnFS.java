package inraito.openerg.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import li.cil.oc.api.fs.FileSystem;
import li.cil.oc.api.fs.Handle;
import li.cil.oc.api.fs.Mode;
import net.minecraft.util.Direction;
import scala.Tuple3;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class IndexMapOnFS {
    public static final String DATA_FOLDER = "$BC_MAPPING";
    public static final String DELIMITER = "/";
    public static final String FILE_MARKER = "F";
    public static final int MAX_TUPLE_LENGTH = 64 * 2;

    public static final String RELATIVE_KEY = "relative";
    public static final String SIDE_KEY = "side";
    public static final String SLOT_KEY = "slot";

    private static Tuple3<Direction, Direction, Integer> fromJson(JsonObject obj){
        Direction relative = Direction.byName(obj.get(RELATIVE_KEY).getAsString());
        Direction side = Direction.byName(obj.get(SIDE_KEY).getAsString());
        int slot = obj.get(SLOT_KEY).getAsInt();
        return new Tuple3<>(relative, side, slot);
    }

    private static JsonObject toJson(Tuple3<Direction, Direction, Integer> tuple){
        JsonObject obj = new JsonObject();
        obj.addProperty(RELATIVE_KEY, tuple._1().toString());
        obj.addProperty(SIDE_KEY, tuple._2().toString());
        obj.addProperty(SLOT_KEY, tuple._3());
        return obj;
    }

    @Nullable
    public static Tuple3<Direction, Direction, Integer> get(FileSystem fs, int key){
        String path = parsePath(key);
        try {
            Handle handle = fs.getHandle(fs.open(path, Mode.Read));
            byte[] data = new byte[MAX_TUPLE_LENGTH];
            handle.read(data);
            handle.close();
            String str = new String(data, 0, MAX_TUPLE_LENGTH, StandardCharsets.UTF_8);
            JsonObject obj = new Gson().fromJson(str, JsonObject.class);
            return fromJson(obj);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /**
     * This method is used to make sure the directories along the desired path and itself exist. So
     * if they don't exist, we'll create it.
     * @param fs file system
     * @param path path to make sure
     * @return true if succeed, the given path is an existent directory now, and false for failure,
     * it's not a directory, probably because some names along the path are already occupied by files.
     */
    private static boolean checkDirectory(FileSystem fs, String path){
        int index = path.lastIndexOf(DELIMITER);
        if(index==-1){
            throw new IllegalArgumentException("illegal path " + path);
        }
        if(index==0 || checkDirectory(fs, path.substring(0, index-1))){
            if(fs.exists(path)){
                return fs.isDirectory(path);
            }else{
                fs.makeDirectory(path);
                return true;
            }
        }
        return false;
    }

    public static boolean put(FileSystem fs, int key, Tuple3<Direction, Direction, Integer> value){
        String path = parsePath(key);
        int index = path.lastIndexOf(DELIMITER);
        if(!checkDirectory(fs, path.substring(0, index-1)) || fs.isDirectory(path)){
            return false;
        }
        try {
            Handle handle = fs.getHandle(fs.open(path, Mode.Write));
            JsonObject obj = toJson(value);
            byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);
            handle.write(data);
            handle.close();
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    public static boolean containsKey(FileSystem fs, int key){
        //TODO: remove the redundant read.
        return get(fs, key) != null;
    }

    public static void remove(FileSystem fs, int key){
        String path = parsePath(key);
        int index = path.lastIndexOf(DELIMITER);
        if(!checkDirectory(fs, path.substring(0, index-1)) || fs.isDirectory(path) || !fs.exists(path)){
            return;
        }
        fs.delete(path);
    }

    private static void delete(FileSystem fs, String path){
        if(!fs.isDirectory(path)){
            fs.delete(path);
            return;
        }
        for(String name : fs.list(path)){
            delete(fs, path + DELIMITER + name);
        }
    }

    public static void clear(FileSystem fs){
        delete(fs, DELIMITER + DATA_FOLDER);
    }

    public static String parsePath(int index){
        String s = String.valueOf(index);
        StringBuilder builder = new StringBuilder();
        builder.append(DELIMITER + DATA_FOLDER + DELIMITER);
        for(char c : s.toCharArray()){
            builder.append(c);
            builder.append(DELIMITER);
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(FILE_MARKER);
        return builder.toString();
    }
}
