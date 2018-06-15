package net.jr.collection;

import java.util.HashMap;
import java.util.Map;

public final class CollectionsUtil {

    public static <K, V> Map<K, V> zip(K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("keys.length != values.length");
        }
        HashMap<K, V> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

}