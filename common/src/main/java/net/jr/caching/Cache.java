package net.jr.caching;

import net.jr.converters.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface Cache<K, T> {

    T get(K key);

    void put(K key, T data);

    void evict(K key);

    void evictAll();

    class Builder<K, V> {

        private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

        private static final Logger getLog() {
            return LOGGER;
        }

        private int ttl = 10;

        private TimeUnit timeUnit = TimeUnit.MINUTES;

        private Callable<Cache<K, V>> callable;

        private Builder() {
        }

        public static <K, V> Builder<K, V> inMemory(Class<K> keyType, Class<V> valueType) {
            Builder<K, V> builder = new Builder<>();
            builder.callable = () -> new MapCache<>(builder.ttl, builder.timeUnit);
            return builder;
        }

        public static Builder<String, byte[]> onDisk(String name) {
            Builder<String, byte[]> builder = new Builder<>();
            builder.callable = () -> new DiskCache(name, builder.ttl, builder.timeUnit);
            return builder;
        }

        public Builder<K, V> fallbackingTo(Builder<K, V> builder) {
            Callable<Cache<K, V>> wrapped = callable;
            callable = () -> {
                final Cache<K, V> cache = wrapped.call();
                final Cache<K, V> fallback = builder.callable.call();

                return new Cache<K, V>() {
                    @Override
                    public V get(K key) {
                        V data = cache.get(key);
                        if (data == null) {
                            data = fallback.get(key);
                            if (data != null) {
                                cache.put(key, data);
                            }
                        }
                        if(getLog().isDebugEnabled()) {
                            if (data == null) {
                                getLog().trace("Cache Miss");
                            } else {
                                getLog().trace("Cache Hit");
                            }
                        }
                        return data;
                    }

                    @Override
                    public void put(K key, V data) {
                        cache.put(key, data);
                        fallback.put(key, data);
                    }

                    @Override
                    public void evict(K key) {
                        cache.evict(key);
                        fallback.evict(key);
                    }

                    @Override
                    public void evictAll() {
                        cache.evictAll();
                        fallback.evictAll();
                    }
                };
            };
            return this;
        }

        public Builder<K, V> withTtl(int ttl, TimeUnit timeUnit) {
            this.ttl = ttl;
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder<K, V> withFactory(Function<K, V> fnct) {
            Callable<Cache<K, V>> wrapped = callable;
            callable = () -> {
                final Cache<K, V> c = wrapped.call();
                return new Cache<K, V>() {

                    @Override
                    public V get(K key) {
                        V data = c.get(key);
                        if (data == null) {
                            data = fnct.apply(key);
                            put(key, data);
                        }
                        return data;
                    }

                    @Override
                    public void put(K key, V data) {
                        c.put(key, data);
                    }

                    @Override
                    public void evictAll() {
                        c.evictAll();
                    }

                    @Override
                    public void evict(K key) {
                        c.evict(key);
                    }
                };
            };
            return this;
        }

        public Cache<K, V> build() {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public <X> Builder<X, V> withKeyMapper(Function<X, K> mappingFnct) {
            Builder<X, V> builder = new Builder<>();
            builder.ttl = ttl;
            builder.timeUnit = timeUnit;
            Callable<Cache<K, V>> wrapped = callable;
            builder.callable = () -> {
                Cache<K, V> cache = wrapped.call();
                return new Cache<X, V>() {

                    @Override
                    public V get(X key) {
                        K mappedKey = mappingFnct.apply(key);
                        return cache.get(mappedKey);
                    }

                    @Override
                    public void put(X key, V data) {
                        K mappedKey = mappingFnct.apply(key);
                        cache.put(mappedKey, data);
                    }

                    @Override
                    public void evict(X key) {
                        K mappedKey = mappingFnct.apply(key);
                        cache.evict(mappedKey);
                    }

                    @Override
                    public void evictAll() {
                        cache.evictAll();
                    }
                };
            };
            return builder;
        }

        public <X> Builder<K, X> withValueConverter(Converter<X, V> converter) {
            Builder<K, X> builder = new Builder<>();
            builder.ttl = ttl;
            builder.timeUnit = timeUnit;
            Callable<Cache<K, V>> wrapped = callable;
            builder.callable = () -> {

                Cache<K, V> cache = wrapped.call();

                return new Cache<K, X>() {
                    @Override
                    public X get(K key) {
                        V value = cache.get(key);
                        return value == null ? null : converter.convertBack(value);
                    }

                    @Override
                    public void put(K key, X data) {
                        cache.put(key, converter.convert(data));
                    }

                    @Override
                    public void evict(K key) {
                        cache.evict(key);
                    }

                    @Override
                    public void evictAll() {
                        cache.evictAll();
                    }
                };
            };
            return builder;
        }
    }
}
