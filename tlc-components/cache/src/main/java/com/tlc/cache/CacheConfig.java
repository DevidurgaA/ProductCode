package com.tlc.cache;

import javax.cache.event.CacheEntryListener;

/**
 * @author Abishek
 * @version 1.0
 */
public class CacheConfig<K, V>
{
	private int maxSize = -1;
	private long expireAfterAccess = -1;
	private long expireAfterUpdate = -1;
	private long expireAfterCreate = -1;
	private CacheEntryListener<K , V> listener = null;

	public int getMaxSize()
	{
		return maxSize;
	}
	
	public CacheConfig<K, V> setMaxSize(int maxSize)
	{
		this.maxSize = maxSize;
		return this;
	}

	public long getExpireAfterAccess()
	{
		return expireAfterAccess;
	}

	public CacheConfig<K, V> setExpireAfterAccess(long expireAfterAccess)
	{
		this.expireAfterAccess = expireAfterAccess;
		return this;
	}

	public long getExpireAfterUpdate()
	{
		return expireAfterUpdate;
	}

	public CacheConfig<K, V> setExpireAfterUpdate(long expireAfterUpdate)
	{
		this.expireAfterUpdate = expireAfterUpdate;
		return this;
	}

	public long getExpireAfterCreate()
	{
		return expireAfterCreate;
	}

	public CacheConfig<K, V> setExpireAfterCreate(long expireAfterCreate)
	{
		this.expireAfterCreate = expireAfterCreate;
		return this;
	}

	public CacheEntryListener<K , V> getListener()
	{
		return listener;
	}

	public CacheConfig<K, V> setListener(CacheEntryListener<K , V> listener)
	{
		this.listener = listener;
		return this;
	}

}
