package x.tools.framework;

import android.telecom.Call;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    public static String getProcessName(int pid) {
        String processName;
        String pidStr = String.valueOf(pid);
        try {
            File file = new File("/proc/" + pidStr + "/cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            return null;
        }
    }

    private static String processName = null;
    public static String getProcessName() {
        if (processName == null) {
            int pid = android.os.Process.myPid();
            processName = getProcessName(pid);
        }
        return processName;
    }


    private static final Random random = new Random();

    public static int randomRange(int min, int max) {
        if (max <= min) throw new AssertionError("assert fail: max > min");
        int r = random.nextInt();
        return min + (Math.abs(r) % (max - min));
    }

    public static float randomRange(float min, float max) {
        if (max <= min) throw new AssertionError("assert fail: max > min");
        float r = random.nextFloat();
        return min + (Math.abs(r) % (max - min));
    }


    private static final String DEFAULT_RANDOM_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    public static String randomText(int length) {
        return randomText(DEFAULT_RANDOM_CHARACTERS, length);
    }

    public static String randomText(String space, int length) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i)
            sb.append(space.charAt(random.nextInt(space.length())));
        return sb.toString();
    }
}
