package com.tlc.sql.api.dml;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class OrderByClause
{
	public enum OrderType
	{
		ASCENDING, DESCENDING
	}
	
	private final Column column;
	
	private final OrderType sortType;

	private boolean isNullFirst = true;
	
	public OrderByClause(Column column)
	{
		this(column, OrderType.ASCENDING);
	}
	
	public OrderByClause(Column column, OrderType type)
	{
		this.sortType = Objects.requireNonNull(type);
		this.column = Objects.requireNonNull(column);
	}
	
	public Column getColumn()
	{
		return column;
	}

	public OrderType getType()
	{
		return sortType;
	}

	public boolean isNullFirst()
	{
		return isNullFirst;
	}

	public void setNullFirst(boolean isNUllFirst)
	{
		this.isNullFirst = isNUllFirst;
	}

	
}
