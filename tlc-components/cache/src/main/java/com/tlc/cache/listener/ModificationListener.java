package com.tlc.cache.listener;

import javax.cache.event.*;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class ModificationListener<K, V> extends CacheListener<K, V>
{
    @Override
    public final void onCreated(K key, V value) throws CacheEntryListenerException
    {
        put(key, value);
    }

    @Override
    public final void onRemoved(K key, V value) throws CacheEntryListenerException
    {
        remove(key, value);
    }

    @Override
    public final void onExpired(K key, V value) throws CacheEntryListenerException
    {
        remove(key, value);
    }

    @Override
    public final void onUpdated(K key, V value, V oldValue) throws CacheEntryListenerException
    {
        put(key, value);
    }

    public abstract void put(K key, V value);

    public abstract void remove(K key, V value);
}
