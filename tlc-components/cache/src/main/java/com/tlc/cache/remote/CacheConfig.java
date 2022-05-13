package com.tlc.cache.remote;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class CacheConfig
{
	private final String name;
	private int ttl = -1;
	private int size = -1;

	public CacheConfig(String name)
	{
		this.name = Objects.requireNonNull(name);
	}

	public String getName()
	{
		return name;
	}

	public int getTtl()
	{
		return ttl;
	}

	public void setTtl(int ttl)
	{
		this.ttl = ttl;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}
}
