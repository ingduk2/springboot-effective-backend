package com.effective.backend.config.hystrix;

import com.netflix.hystrix.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

@Slf4j
public class HystrixClearCommand extends HystrixCommand {

    private final Cache globalCache;
    private final Cache localCache;

    public HystrixClearCommand(Cache localCache, Cache globalCache) {
        super(HystrixKey.getKey("clear"));
        this.globalCache = globalCache;
        this.localCache = localCache;
    }

    @Override
    protected Object run() {
        localCache.clear();
        globalCache.clear();
        return null;
    }

    @Override
    protected Object getFallback() {
        log.warn("cache clear fallback called, circuit is {}", super.circuitBreaker.isOpen());
        localCache.clear();
        return null;
    }
}
