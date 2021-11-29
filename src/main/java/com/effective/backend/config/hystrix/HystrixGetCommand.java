package com.effective.backend.config.hystrix;

import com.netflix.hystrix.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

@Slf4j
public class HystrixGetCommand extends HystrixCommand<ValueWrapper> {

    private final Cache globalCache;
    private final Object key;

    public HystrixGetCommand(Cache globalCache, Object key) {
        super(HystrixKey.getKey("get"));
        this.globalCache = globalCache;
        this.key = key;
    }


    @Override
    protected ValueWrapper run() {
        log.info("global get");
        return globalCache.get(key);
    }

    @Override
    protected ValueWrapper getFallback() {
        log.warn("get fallback called, circuit is {}", super.circuitBreaker.isOpen());
        return null;
    }
}
