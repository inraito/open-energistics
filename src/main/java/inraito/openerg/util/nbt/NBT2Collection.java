package inraito.openerg.util.nbt;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NBT2Collection {
    public static Map<String, Object> toMap(CompoundNBT nbt){
        Map<String, Object> res = new HashMap<>();
        for(String key : nbt.getAllKeys()){
            INBT t = nbt.get(key);
            try {
                res.put(key, cast2Object(t));
            }catch (Exception ignored){}//just skip those unrecognized nbt.
        }
        return res;
    }

    public static List<Object> toList(ListNBT list){
        List<Object> res = new ArrayList<>();
        for(INBT inbt : list){
            try {
                res.add(cast2Object(inbt));
            }catch (Exception ignored){}//just skip those unrecognized nbt.
        }
        return res;
    }

    public static Object cast2Object(INBT t){
        if(t instanceof StringNBT){
            return ((StringNBT) t).getAsString();
        }else if(t instanceof CompoundNBT){
            return toMap((CompoundNBT) t);
        }else if(t instanceof IntNBT){
            return ((IntNBT) t).getAsInt();
        }else if(t instanceof FloatNBT){
            return ((FloatNBT) t).getAsDouble();
        }else if(t instanceof ListNBT){
            return toList(((ListNBT) t));
        }
        throw new IllegalArgumentException("INBT not matched!");
    }
}
