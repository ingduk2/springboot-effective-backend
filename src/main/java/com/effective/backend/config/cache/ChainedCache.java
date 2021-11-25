package com.effective.backend.config.cache;

import com.effective.backend.config.hystrix.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class ChainedCache implements Cache {
    
    private final Cache localCache;
    private final Cache globalCache;
    
    public ChainedCache(List<Cache> caches) {
        this.localCache = caches.get(0);
        this.globalCache = caches.get(1);
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = localCache.get(key);
        log.info("local Cache : {}", valueWrapper);
        if (!isEmpty(valueWrapper)) {
            log.info("local Cache LookUp");
            return valueWrapper;
        } else {
            valueWrapper = new HystrixGetCommand(globalCache, key).execute();
            log.info("global cache : {}", valueWrapper);
            if (valueWrapper != null) {
                localCache.put(key, valueWrapper.get());
            }
            return valueWrapper;
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return new HystrixPutIfAbsentCommand(localCache, globalCache, key, value).execute();
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return localCache.evictIfPresent(key);
    }

    @Override
    public boolean invalidate() {
        return localCache.invalidate();
    }

    @Override
    public void put(Object key, Object value) {
        new HystrixPutCommand(localCache, globalCache, key, value).execute();
    }

    @Override
    public void evict(Object key) {
        new HystrixEvictCommand(localCache, globalCache, key);
    }

    @Override
    public void clear() {
        new HystrixClearCommand(localCache, globalCache).execute();
    }

    @Override
    public String getName() {
        if (!localCache.getName().isEmpty()) {
            return localCache.getName();
        }
        return new HystrixGetNameCommand(globalCache).execute();
    }

    @Override
    public Object getNativeCache() {
        throw new RuntimeException("not support Calculation");
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        throw new RuntimeException("not support Calculation");
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new RuntimeException("not support Calculation");
    }

    public void clearLocalCache() {
        localCache.clear();
    }

    public boolean isSynchronized(Object key) {
        ValueWrapper localValue = localCache.get(key);
        ValueWrapper globalValue = new HystrixGetCommand(globalCache, key) {
            @Override
            protected ValueWrapper getFallback() {
                log.warn("Synchronize get fallback called, circuit is {}", super.circuitBreaker.isOpen());
                return localValue;
            }
        }.execute();

        if (isEmpty(localValue)) {
            return true;
        } else if (isEmpty(globalValue)) {
            return false;
        }

        return localValue.get().equals(globalValue.get());
    }

    private boolean isEmpty(ValueWrapper valueWrapper) {
        return valueWrapper == null;
    }

}
