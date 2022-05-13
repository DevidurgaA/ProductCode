package com.tlc.cache.remote;

import com.tlc.cache.Cache;

/**
 * @author Abishek
 * @version 1.0
 */
public interface CacheService
{
	<D, E> Cache<D, E> createOrOpenCache(CacheConfig config);
}
