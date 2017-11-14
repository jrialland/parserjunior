package net.jr.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapCache<K, V> implements Cache<K, V> {

    private class CacheEntry {
        V data;
        long lastUpdate;
    }

    private long maxAge;

    private Map<K, CacheEntry> entries = new HashMap<>();

    public MapCache(int ttl, TimeUnit timeUnit) {
        maxAge = timeUnit.toMillis(ttl);
    }

    @Override
    public V get(K key) {
        V data = null;
        CacheEntry entry = entries.get(key);
        if (entry != null) {
            long now = System.currentTimeMillis();
            if (now - entry.lastUpdate > maxAge) {
                entries.remove(key);
            } else {
                entry.lastUpdate = now;
                data = entry.data;
            }
        }
        return data;
    }

    @Override
    public void put(K key, V data) {
        CacheEntry entry = new CacheEntry();
        entry.data = data;
        entry.lastUpdate = System.currentTimeMillis();
        entries.put(key, entry);
    }
}
