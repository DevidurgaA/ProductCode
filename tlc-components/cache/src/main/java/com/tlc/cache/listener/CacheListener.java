package com.tlc.cache.listener;

import javax.cache.event.*;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CacheListener<K, V> implements CacheEntryCreatedListener<K, V>, CacheEntryRemovedListener<K, V>, CacheEntryExpiredListener<K, V>, CacheEntryUpdatedListener<K, V>
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
            onExpired(key, value);
        }

    }

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

    protected abstract void onUpdated(K key, V value, V oldValue);

    protected abstract void onExpired(K key, V value);

    protected abstract void onRemoved(K key, V value);

    protected abstract void onCreated(K key, V value);

}
