package com.tlc.cache.listener;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class ExpiryListener<K, V> implements CacheEntryExpiredListener<K, V>
{
    @Override
    public final void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents) throws CacheEntryListenerException
    {
        for(CacheEntryEvent<? extends K, ? extends V> event : cacheEntryEvents)
        {
            final K key = event.getKey();
            final V value = event.getValue();
            onExpired(key, value);
        }

    }

    public abstract void onExpired(K key, V value);
}
