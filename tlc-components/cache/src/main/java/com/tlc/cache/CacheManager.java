package com.tlc.cache;

import com.tlc.cache.internal.LocalHeapCache;
import com.tlc.cache.internal.SoftCache;
import com.tlc.cache.internal.WeakCache;

/**
 * @author Abishek
 * @version 1.0
 */
public class CacheManager
{
    private static class Helper
    {
        private static final CacheManager INSTANCE = new CacheManager();
    }

    public static CacheManager getInstance()
    {
        return Helper.INSTANCE;
    }

    private CacheManager(){}

    public <D, E> Cache<D, E> createCache()
    {
        return new LocalHeapCache<D, E>(new CacheConfig<>()).init();
    }

    public <D, E> Cache<D, E> createSoftCache()
    {
        return new SoftCache<D, E>(new CacheConfig<>()).init();
    }

    public <D, E> Cache<D, E> createWeakCache()
    {
        return new WeakCache<D, E>(new CacheConfig<>()).init();
    }

    public <D, E> Cache<D, E> createCache(CacheConfig<D, E> config)
    {
        return new LocalHeapCache<>(config).init();
    }

    public <D, E> Cache<D, E> createSoftCache(CacheConfig<D, E> config)
    {
        return new SoftCache<>(config).init();
    }

    public <D, E> Cache<D, E> createWeakCache(CacheConfig<D, E> config)
    {
        return new WeakCache<>(config).init();
    }
}
