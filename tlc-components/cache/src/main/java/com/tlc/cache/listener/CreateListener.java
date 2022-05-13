package com.tlc.cache.listener;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CreateListener<K, V> implements CacheEntryCreatedListener<K, V>
{
    @Override
    public final void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents) throws CacheEntryListenerException
    {
        for(CacheEntryEvent<? extends K, ? extends V> event : cacheEntryEvents)
        {
            final K key = event.getKey();
            final V value = event.getValue();
            onCreated(key, value);
        }
    }

    public abstract void onCreated(K key, V value);
}
