package com.tlc.cache.listener;

import javax.cache.event.*;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class RemoveExpiryListener<K, V> implements CacheEntryRemovedListener<K, V>, CacheEntryExpiredListener<K, V>
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

    @Override
    public final void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents) throws CacheEntryListenerException
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
