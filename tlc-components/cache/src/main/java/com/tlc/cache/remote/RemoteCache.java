package com.tlc.cache.remote;

import com.tlc.cache.Cache;

import java.util.concurrent.locks.Lock;

/**
 * @author Abishek
 * @version 1.0
 */
public interface RemoteCache<K, V> extends Cache<K, V>
{
    boolean putIfAbsent(K key, V value);

    boolean replace(K key, V oldValue, V newValue);

    Lock lock(K key);
}
