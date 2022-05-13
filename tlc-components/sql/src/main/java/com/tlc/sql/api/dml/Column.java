package com.tlc.sql.api.dml;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.internal.status.SQLErrorCodes;
import com.tlc.sql.api.meta.ColumnDefinition;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class Column implements Comparable<Column>
{
	private final Table table;
	private final String columnName;
	private final String columnAlias;
	private final ColumnType columnType;
	
	private Integer hashCode = null;
	private final ColumnDefinition columnDefinition;
	private final boolean hasAlias;
	private final boolean isPkColumn;

	public enum ColumnType
	{
		NORMAL(0), COUNT(1), MAX(2), MIN(3), AVERAGE(4), DISTINCT(5);
		private final int order;
		ColumnType(int order)
		{
			this.order = order;
		}
		
		int getOrder()
		{
			return order;
		}
	}

	Column(Table table, String name, String alias, boolean isPkColumn)
	{
		this.columnDefinition = table.getTableDefinition().getColumnDefinition(name);
		if(columnDefinition == null)
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_UNKNOWN_COLUMN, name);
		}
		this.isPkColumn = isPkColumn;
		this.table = Objects.requireNonNull(table);
		this.columnName = Objects.requireNonNull(name).trim();
		this.columnAlias = Objects.requireNonNull(alias).trim();
		this.columnType = ColumnType.NORMAL;
		this.hasAlias = !columnName.equals(columnAlias);
	}

	private Column(Column column, ColumnType type)
	{
		this.table = column.table;
		this.isPkColumn = column.isPkColumn;
		this.hasAlias = type != ColumnType.NORMAL;
		this.columnName = column.columnName;
		this.columnAlias = column.columnAlias;
		this.columnType = Objects.requireNonNull(type);
		this.columnDefinition = column.columnDefinition;
	}

	public boolean isPkColumn()
	{
		return isPkColumn;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public String getColumnAlias()
	{
		return columnAlias;
	}

	public boolean hasAlias()
	{
		return hasAlias;
	}

	public Table getTable()
	{
		return table;
	}
	
	public String getTableName()
	{
		return table.getName();
	}

	public Column count()
	{
		return new Column(this , ColumnType.COUNT);
	}
	
	public Column max()
	{
		return new Column(this , ColumnType.MAX);
	}

	public Column min()
	{
		return new Column(this , ColumnType.MIN);
	}

	public Column average()
	{
		return new Column(this , ColumnType.AVERAGE);
	}

	public Column distinct()
	{
		return new Column(this , ColumnType.DISTINCT);
	}

	public ColumnType getColumnType()
	{
		return columnType;
	}

	@Override
	public String toString()
	{
		if(columnName.equals(columnAlias))
		{
			return columnAlias;
		}
		else
		{
			return columnName+"_"+columnAlias;
		}
	}

	@Override
	public boolean equals(Object object)
	{
		if(object instanceof Column)
		{
			return this.hashCode() == object.hashCode();
		}
		return false;
	}
	@Override
	public int hashCode()
	{
		if(hashCode == null)
		{
		    final int prime = 31;
		    int result = 1;
		    result = prime * result + table.hashCode();
		    result = prime * result + columnName.hashCode();
		    result = prime * result + columnAlias.hashCode();
		    result = prime * result + columnType.order;
		    this.hashCode = result;
		}
		return hashCode;
	}

	@Override
	public int compareTo(Column column)
	{
		if(column.columnType.order > this.columnType.order || column.columnType.order == this.columnType.order)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}

	public ColumnDefinition getColumnDefinition()
	{
		return columnDefinition;
	}
}
