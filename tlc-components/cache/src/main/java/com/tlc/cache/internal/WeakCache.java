package com.tlc.cache.internal;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheConfig;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author Abishek
 * @version 1.0
 */
public class WeakCache<K, T> extends LocalHeapCache<K, T>
{
	public WeakCache(CacheConfig<K , T> config)
	{
		super(config);
	}

	@Override
	public Cache<K, T> init()
	{
		final Caffeine<Object, Object> cacheBuilder = getCacheBuilder();
		super.cache = cacheBuilder.weakValues().build();
		return this;
	}

}
