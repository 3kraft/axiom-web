package org.zalando.axiom.web.util;

import java.util.Map;
import java.util.function.Supplier;

public class Util {

    public static <K, V> V getOrPut(Map<K, V> map, K key, Supplier<V> supplier) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            V value = supplier.get();
            map.put(key, value);
            return value;
        }
    }

}
