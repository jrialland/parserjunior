package net.jr.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapCache<K, V> implements Cache<K, V> {


    private static final Logger LOGGER = LoggerFactory.getLogger(MapCache.class);
    private long maxAge;
    private int maxItems;
    private Map<K, CacheEntry> entries = new HashMap<>();

    public MapCache(int ttl, TimeUnit timeUnit) {
        maxAge = timeUnit.toMillis(ttl);
    }

    private static final Logger getLog() {
        return LOGGER;
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
        if (data == null) {
            getLog().trace("MapCache Miss");
        } else {
            getLog().trace("MapCache Hit");
        }
        return data;
    }

    @Override
    public void put(K key, V data) {
        getLog().trace("MapCache Put");
        CacheEntry entry = new CacheEntry();
        entry.data = data;
        entry.lastUpdate = System.currentTimeMillis();
        entries.put(key, entry);
    }

    @Override
    public void evict(K key) {
        getLog().trace("MapCache Evict");
        entries.remove(key);
    }

    @Override
    public void evictAll() {
        getLog().trace("MapCache EvictAll");
        entries.clear();
    }

    private class CacheEntry {
        V data;
        long lastUpdate;
    }
}
