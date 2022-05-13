package com.tlc.sql.api.meta;

import java.util.Objects;
import java.util.Set;


/**
 * @author Abishek
 * @version 1.0
 */
public class UKDefinition
{
	private final Set<String> columns;
	private final String name;
	
	public UKDefinition(String name, Set<String> columns)
	{
		this.name = Objects.requireNonNull(name);
		this.columns = Set.copyOf(columns);
	}
	
	public Set<String> getColumns()
	{
		return columns;
	}

	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name + "=" + columns;
	}
}
