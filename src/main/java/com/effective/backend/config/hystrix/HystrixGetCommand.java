package com.effective.backend.config.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
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
    protected ValueWrapper run() throws Exception {
        log.info("global get");
        return globalCache.get(key);
    }

    @Override
    protected ValueWrapper getFallback() {
        log.warn("get fallback called, circuit is {}", super.circuitBreaker.isOpen());
        return null;
    }
}
