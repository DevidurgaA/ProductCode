package com.tlc.cache.internal;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheConfig;
import com.tlc.cache.listener.CreateListener;
import com.tlc.cache.listener.ExpiryListener;
import com.tlc.cache.listener.RemoveListener;
import com.tlc.cache.listener.UpdateListener;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import javax.cache.event.CacheEntryListener;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Abishek
 * @version 1.0
 */
public class LocalHeapCache<K, V> implements Cache<K , V>
{
	protected com.github.benmanes.caffeine.cache.Cache<K, V> cache;
	private final CacheEntryListener<K, V> listener;
	private final CacheConfig<K , V> config;

	public LocalHeapCache(CacheConfig<K, V> config)
	{
		this.config = Objects.requireNonNull(config);
		this.listener = config.getListener();
	}

	@Override
	public V get(K key)
	{
		return cache.getIfPresent(key);
	}

	@Override
	public boolean containsKey(K key)
	{
		return cache.getIfPresent(key) != null;
	}

	@Override
	public void clear()
	{
		cache.cleanUp();
	}

	@Override
	public Map<K, V> getAll(Set<K> keys)
	{
		return cache.getAllPresent(keys);
	}

	@Override
	public void putAll(Map<K, V> newMap)
	{
		this.cache.putAll(newMap);
	}

	@Override
	public void removeAll(Set<K> keys)
	{
		keys.forEach(this::remove);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		return cache.get(key, mappingFunction);
	}

	@Override
	public void put(K key, V data)
	{
		if(listener != null)
		{
			final V element = get(key);
			cache.put(key, data);
			if(element != null)
			{
				if(listener instanceof CreateListener)
				{
					((CreateListener<K, V>)listener).onCreated(key, data);
				}
			}
			else
			{
				if(listener instanceof UpdateListener)
				{
					((UpdateListener<K, V>)listener).onUpdated(key, data, null);
				}
			}
		}
		else
		{
			cache.put(key, data);
		}
	}

	@Override
	public V remove(K key)
	{
		final V element = get(key);
		if(element != null)
		{
			cache.invalidate(key);
			if(listener instanceof RemoveListener)
			{
				((RemoveListener<K, V>)listener).onRemoved(key, element);
			}
		}
		return element;
	}

	@Override
	public void forEach(BiConsumer<K, V> consumer)
	{
		cache.asMap().forEach(consumer);
	}


	public Cache<K, V> init()
	{
		final Caffeine<Object, Object> cacheBuilder = getCacheBuilder();
		this.cache = cacheBuilder.build();
		return this;
	}

	protected Caffeine<Object, Object> getCacheBuilder()
	{
		final Caffeine<Object, Object> builder = Caffeine.newBuilder();
		final long maxSize = config.getMaxSize();
		if(maxSize > 0)
		{
			builder.maximumSize(maxSize);
		}
		final long expireAfterAccess = config.getExpireAfterAccess();
		if(expireAfterAccess > 0)
		{
			builder.expireAfterAccess(expireAfterAccess, TimeUnit.MILLISECONDS);
		}
		final long expireAfterWrite = config.getExpireAfterUpdate();
		if(expireAfterWrite > 0)
		{
			builder.expireAfterWrite(expireAfterWrite, TimeUnit.MILLISECONDS);
		}
		final CacheEntryListener<K , V> listener = config.getListener();
		if(listener != null)
		{
			if(listener instanceof final ExpiryListener<K, V> entryExpiredListener)
			{
				builder.removalListener((RemovalListener<K, V>) (k, v, removalCause) ->
				{
					if(removalCause == RemovalCause.EXPIRED)
					{
						entryExpiredListener.onExpired(k, v);
					}
				});
			}
		}
		return builder;
	}


}
