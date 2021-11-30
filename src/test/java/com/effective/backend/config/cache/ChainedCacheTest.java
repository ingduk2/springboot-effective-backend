package com.effective.backend.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
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
        given(caches.get(eq(0))).willReturn(localCache);
        given(caches.get(eq(1))).willReturn(globalCache);
        cache = new ChainedCache(caches);
    }

    @Test
    @DisplayName("local cache가 없다면 global cache 사용")
    void useGlobalCache() {
        //given
        String key = "key1";
        String value = "value1";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);

        given(localCache.get(eq(key))).willReturn(null);
        given(globalCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);

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

        given(localCache.get(eq(key))).willReturn(null);
        given(globalCache.get(eq(key))).willReturn(globalValueWrapper);
        given(globalValueWrapper.get()).willReturn(value);
        given(localValueWrapper.get()).willReturn(null);

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

        given(localCache.get(eq(key))).willReturn(null);
        given(globalCache.get(eq(key))).willReturn(null);
        given(valueWrapper.get()).willReturn(value);

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

        given(localCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);

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

        given(localCache.get(eq(key))).willReturn(null);
        given(globalCache.get(eq(key))).willThrow(new RuntimeException());
        given(valueWrapper.get()).willReturn(value);

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

        given(localCache.get(eq(key))).willReturn(null);
        given(globalCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);

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

    @Test
    @DisplayName("로컬 캐시에 값이 없으면 동기화가 필요 없다.")
    void cacheSynchronized1() {
        //given
        String key = "key";
        String value = "value";
        given(localCache.get(eq(key))).willReturn(null);

        //when
        boolean result = cache.isSynchronized(key);

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("로컬 캐시가 있고 글로벌 캐시가 없으면 동기화가 필요하다")
    void cacheSynchronized3() {
        //given
        String key = "key";
        String value = "value";

        ValueWrapper localValue = mock(ValueWrapper.class);
        ValueWrapper globalValue = mock(ValueWrapper.class);
        given(localCache.get(eq(key))).willReturn(localValue);
        given(localValue.get()).willReturn(value);
        given(globalCache.get(eq(key))).willReturn(globalValue);
        given(globalValue.get()).willReturn(null);

        //when
        boolean result = cache.isSynchronized(key);

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("로컬 캐시와 글로벌 캐시와 값이 같으면 동기화가 필요 없다.")
    void cacheSynchronized5() {
        //given
        String key = "key";
        String value = "value";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);
        given(localCache.get(eq(key))).willReturn(valueWrapper);
        given(globalCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);

        //when
        boolean result = cache.isSynchronized(key);

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("로컬 캐시와 글로벌 캐시의 값이 다르면 동기화가 필요하다.")
    void cacheSynchronized6() {
        //given
        String key = "key";
        String value = "value";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);
        given(localCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);
        given(globalCache.get(eq(key))).willReturn(null);

        //when
        boolean result = cache.isSynchronized(key);

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("글로벌 캐시에 장애가 발생하면 동기화가 필요없다.")
    void cacheSynchronized7() {
        //given
        String key = "key";
        String value = "value";
        ValueWrapper valueWrapper = mock(ValueWrapper.class);
        given(localCache.get(eq(key))).willReturn(valueWrapper);
        given(valueWrapper.get()).willReturn(value);
        given(globalCache.get(eq(key))).willThrow(new RuntimeException());

        //when
        boolean result = cache.isSynchronized(key);

        //then
        assertTrue(result);
    }


}