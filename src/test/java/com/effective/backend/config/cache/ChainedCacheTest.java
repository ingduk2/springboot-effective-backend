package com.effective.backend.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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

    private ChainedCache cache;

    @BeforeEach
    void setUp() {
        List<Cache> caches = mock(List.class);
        when(caches.get(eq(0))).thenReturn(localCache);
        when(caches.get(eq(1))).thenReturn(globalCache);
        cache = new ChainedCache(caches);
    }

    @Test
    @DisplayName("local cache가 없다면 global cache 사용")
    void useGlobalCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertEquals(result, valueWrapper);
    }

    @Test
    @DisplayName("local cache에 value가 없다면 global cache를 사용한다")
    void useGlobalCache2() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper localValueWrapper = mock(ValueWrapper.class);
        ValueWrapper globalValueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenReturn(globalValueWrapper);
        when(globalValueWrapper.get()).thenReturn(value);
        when(localValueWrapper.get()).thenReturn(null);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertEquals(result, globalValueWrapper);
    }

    @Test
    @DisplayName("둘 다 cache가 없다면 null을 반환한다.")
    void nullCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenReturn(null);
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertNull(result);
    }

    @Test
    @DisplayName("local cache가 있다면 local cache를 사용한다")
    void useLocalCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertEquals(result, valueWrapper);
    }

    @Test
    @DisplayName("global cache에 오류가 있다면 fallback이 발동된다.")
    void useFallback() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenThrow(new RuntimeException());
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertNull(result);
    }

    @Test
    @DisplayName("global cache가 존재하면 local cache에 저장한다.")
    void putCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        when(localCache.get(eq(key))).thenReturn(null);
        when(globalCache.get(eq(key))).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(value);

        //when
        ValueWrapper result = cache.get(key);

        //then
        assertEquals(result, valueWrapper);
        verify(localCache, times(1)).put(eq(key), eq(value));
    }

    @Test
    @DisplayName("cache를 초기화하면 local, global cache 둘 다 초기화된다.")
    void clearCache() {
        //given beforeEach
        //when
        cache.clear();

        //then
        verify(localCache, times(1)).clear();
        verify(globalCache, times(1)).clear();
    }


}