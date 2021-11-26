package com.effective.backend.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.cache.Cache.*;

@Slf4j
@ExtendWith(SpringExtension.class)
class ChainedCacheTest {

    @Mock
    private Cache localCache;

    @Mock
    private Cache globalCache;

    @Test
    @DisplayName("local cache가 없다면 global cache 사용")
    void useGlobalCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);
        List<Cache> caches = mock(List.class);
        when(caches.get(eq(0))).thenReturn(localCache);
        when(caches.get(eq(1))).thenReturn(globalCache);
        ChainedCache cache = new ChainedCache(caches);
        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertEquals(result, valueWrapper);
    }
}