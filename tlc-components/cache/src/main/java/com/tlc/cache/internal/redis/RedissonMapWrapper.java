package com.tlc.cache.internal.redis;

import com.tlc.cache.remote.RemoteCache;
import org.redisson.api.RMapCache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Abishek
 * @version 1.0
 */
class RedissonMapWrapper<K, V> implements RemoteCache<K, V>
{
    private final RMapCache<K, V> map;
    private final long ttl;

    RedissonMapWrapper(RMapCache<K, V> map, long ttl)
    {
        this.ttl = ttl;
        this.map = map;
    }

    @Override
    public void put(K key, V value)
    {
        if(ttl != -1)
        {
            map.fastPut(key, value, ttl, TimeUnit.MILLISECONDS);
        }
        else
        {
            map.fastPut(key, value);
        }
    }

    @Override
    public V remove(K key)
    {
        return map.remove(key);
    }

    @Override
    public V get(K key)
    {
        return map.get(key);
    }

    @Override
    public boolean containsKey(K key)
    {
        return map.containsKey(key);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public void putAll(Map<K, V> newMap)
    {
        if(ttl != -1)
        {
            map.putAll(newMap, ttl, TimeUnit.MILLISECONDS);
        }
        else
        {
            map.putAll(newMap);
        }
    }

    @Override
    public void removeAll(Set<K> keys) {

    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
    {
        final V value = get(key);
        if(value == null)
        {
            if(ttl != -1)
            {
                return map.putIfAbsent(key, mappingFunction.apply(key), ttl, TimeUnit.MILLISECONDS);
            }
            else
            {
                return map.putIfAbsent(key, mappingFunction.apply(key));
            }
        }
        else
        {
            return value;
        }
    }

    @Override
    public Map<K, V> getAll(Set<K> keys)
    {
        return map.getAll(keys);
    }

    @Override
    public boolean putIfAbsent(K key, V value)
    {
        return map.putIfAbsent(key, value) == value;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public Lock lock(K key)
    {
        return map.getLock(key);
    }

    @Override
    public void forEach(BiConsumer<K, V> consumer)
    {
        map.forEach(consumer);
    }
}
