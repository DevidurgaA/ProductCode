package com.tlc.cache.listener;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class RemoveListener<K, V> implements CacheEntryRemovedListener<K, V>
{
    @Override
    public final void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents) throws CacheEntryListenerException
    {
        for(CacheEntryEvent<? extends K, ? extends V> event : cacheEntryEvents)
        {
            final K key = event.getKey();
            final V value = event.getValue();
            onRemoved(key, value);
        }
    }

    public abstract void onRemoved(K key, V value);
}
