package net.jr.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BidiMap<K, V> implements Map<K, V> {

    private Map<K, V> map;

    private Map<V, K> reverseMap;

    public BidiMap() {
        this(HashMap.class);
    }

    @SuppressWarnings(("unchecked"))
    public BidiMap(Class<? extends Map> mapType) {
        try {
            this.map = (Map) mapType.newInstance();
            this.reverseMap = (Map) mapType.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return reverseMap.containsKey(o);
    }

    @Override
    public V get(Object o) {
        return map.get(o);
    }

    public K getForValue(V value) {
        return reverseMap.get(value);
    }

    @Override
    public V put(K k, V v) {
        reverseMap.put(v, k);
        return map.put(k, v);
    }

    @Override
    public V remove(Object o) {
        V v = map.remove(o);
        reverseMap.remove(v);
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.entrySet().stream().forEach(entry -> {
            K key = entry.getKey();
            V value = entry.getValue();
            put(key, value);
        });
    }

    @Override
    public void clear() {
        reverseMap.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return reverseMap.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(BidiMap.class)) {
            return false;
        }
        BidiMap other = (BidiMap) o;
        return map.equals(other.map) && reverseMap.equals(other.reverseMap);
    }

    @Override
    public int hashCode() {
        return reverseMap.hashCode() + map.hashCode();
    }

    @Override
    public V getOrDefault(Object o, V v) {
        return map.getOrDefault(o, v);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> biConsumer) {
        map.forEach(biConsumer);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction) {
        map.replaceAll(biFunction);
    }

    @Override
    public V putIfAbsent(K k, V v) {
        return map.putIfAbsent(k, v);
    }

    @Override
    public boolean remove(Object o, Object o1) {
        boolean r = map.remove(o, o1);
        if (r) {
            reverseMap.remove(o1);
        }
        return r;
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        boolean removed = map.remove(k, v);
        if (removed) {
            reverseMap.remove(v);
        }
        map.put(k, v1);
        return removed;
    }

    @Override
    public V replace(K k, V v) {
        V oldV = map.remove(k);
        if (oldV != null) {
            reverseMap.remove(oldV);
        }
        map.put(k, v);
        return oldV;
    }

    @Override
    public V computeIfAbsent(K k, Function<? super K, ? extends V> function) {
        return map.computeIfAbsent(k, function);
    }

    @Override
    public V computeIfPresent(K k, BiFunction<? super K, ? super V, ? extends V> biFunction) {
        return map.computeIfPresent(k, biFunction);
    }

    @Override
    public V compute(K k, BiFunction<? super K, ? super V, ? extends V> biFunction) {
        return map.compute(k, biFunction);
    }

    @Override
    public V merge(K k, V v, BiFunction<? super V, ? super V, ? extends V> biFunction) {
        return map.merge(k, v, biFunction);
    }

}
