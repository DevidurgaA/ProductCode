package com.tlc.cache.listener;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class UpdateListener<K, V> implements CacheEntryUpdatedListener<K, V>
{
    @Override
    public final void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents) throws CacheEntryListenerException
    {
        for(CacheEntryEvent<? extends K, ? extends V> event : cacheEntryEvents)
        {
            final K key = event.getKey();
            final V value = event.getValue();
            final V oldValue = event.getOldValue();
            onUpdated(key, value, oldValue);
        }
    }

    public abstract void onUpdated(K key, V value, V oldValue);
}
