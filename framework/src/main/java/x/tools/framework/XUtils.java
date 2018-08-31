package x.tools.framework;

import android.telecom.Call;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public final class XUtils {
    public static int normalizeId(int id) {
        return (id) & 0x0000FFFF;
    }

    public static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static <T> boolean isEmptyArray(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmptyList(List list) {
        return list == null || list.size() == 0;
    }

    public static <K, V> V mapGetOrDefault(Map<K, V> map, K key, Callable<V> createNew) throws Exception {
        if (map == null) {
            return null;
        } else {
            V value = map.get(key);
            if (value == null) {
                value = createNew.call();
                map.put(key, value);
            }
            return value;
        }
    }

}
