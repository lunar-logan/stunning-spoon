package sage.util;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Dell on 02-06-2016.
 */
public class MemCache {
    private static HashMap<Integer, Object> map = new HashMap<>();

    public static int put(String key, Object obj) {
        int keyHash = key.hashCode();
        map.put(keyHash, obj);
        System.err.println("Added " + obj + " to cache");
        return keyHash;
    }

    public static int put(Object obj) {
        return put(UUID.randomUUID().toString(), obj);
    }

    public static Object get(String key) {
        return map.get(key.hashCode());
    }

    public static Object get(int key) {
        return map.get(key);
    }
}
