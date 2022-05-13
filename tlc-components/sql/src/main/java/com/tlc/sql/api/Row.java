package com.tlc.sql.api;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.dml.Column;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.meta.ImmutableColumns;
import com.tlc.sql.api.meta.PKDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.*;
import java.util.function.BiConsumer;


/**
 * @author Abishek
 * @version 1.0
 */
public class Row
{
	private final Table table;

	private final Map<String, Object> columnValues;
	private final Set<String> modifiedColumns;

	private final Set<String> editableColumns;
	private final Set<String> allColumns;

	private final String pkColumn;
	private final boolean isNewRow;

	public Row(Table table) // New Row
	{
		this(table, true);
		table.getTableDefinition().getEditableColumns().forEach( (column, def) ->
		{
			final Object defaultValue = def.getDefaultValue();
			if(defaultValue != null)
			{
				this.setWithoutCheck(column, defaultValue);
			}
		});
	}

	public Row(Table table, Long pk) // Existing Row
	{
		this(table, false);
		this.setWithoutCheck(pkColumn, pk);
	}

	private Row(Table table, boolean isNewRow)
	{
		this.table = Objects.requireNonNull(table);
		this.columnValues = new HashMap<>();

		final TableDefinition tableDef = table.getTableDefinition();
		final PKDefinition pkDef = tableDef.getPkDefinition();
		this.pkColumn = pkDef.geColumnName();

		this.allColumns = tableDef.getColumnDefinition().keySet();
		this.editableColumns = tableDef.getEditableColumns().keySet();
		if(isNewRow)
		{
			this.isNewRow = true;
			this.modifiedColumns = new HashSet<>();
			if(pkDef.hasSequenceGenerator())
			{
				final long sequence = pkDef.getNextSequence();
				this.setWithoutCheck(pkColumn, sequence);
			}
		}
		else
		{
			this.modifiedColumns = new HashSet<>();
			this.isNewRow = false;
		}
	}

	public Row(Row row)
	{
		this.table = row.table;

		this.editableColumns = row.editableColumns;
		this.allColumns = row.allColumns;

		this.pkColumn = row.pkColumn;
		this.isNewRow = row.isNewRow;

		this.columnValues = new HashMap<>(row.columnValues);
		this.modifiedColumns = new HashSet<>(row.modifiedColumns);
	}

	public boolean isNewRow()
	{
		return isNewRow;
	}

	public <T> T get(Column column)
	{
		return get(column.getColumnAlias());
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String column)
	{
		return (T) columnValues.get(column);
	}

	public void setOrgId(Long orgId)
	{
		if(table.getTableDefinition().getType().isOrgDependent() && isNewRow)
		{
			setWithoutCheck(ImmutableColumns.ORG_ID.getName(), orgId);
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_INVALID_COLUMN, ImmutableColumns.ORG_ID.getName());
		}
	}

	public Long getOrgId()
	{
		if(table.getTableDefinition().getType().isOrgDependent())
		{
			return get(ImmutableColumns.ORG_ID.getName());
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_INVALID_COLUMN, ImmutableColumns.ORG_ID.getName());
		}
	}

	public void set(Column column, Object value)
	{
		if(column.hasAlias())
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_INVALID_COLUMN, "Alias cannot be used");
		}
		final String columnName = column.getColumnName();
		set(columnName, value);
	}

	public void set(String column, Object value)
	{
		if(!editableColumns.contains(column))
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_INVALID_COLUMN, column);
		}

		if(!isNewRow)
		{
			this.modifiedColumns.add(column);
		}
		this.columnValues.put(column, value);
	}

	public void setWithoutCheck(Column column, Object value)
	{
		setWithoutCheck(column.getColumnAlias(), value);
	}

	public void setWithoutCheck(String column, Object value)
	{
		this.columnValues.put(column, value);
	}

	public Long getPKValue()
	{
		return get(pkColumn);
	}

	public Table getTable()
	{
		return table;
	}
	
	public boolean isCompleteRow()
	{
		return columnValues.size() == allColumns.size();
	}

	public void forEach(BiConsumer<String, Object> biConsumer)
	{
		columnValues.forEach(biConsumer);
	}

	public void merge(Row row)
	{
		if(row.getTable().equals(this.getTable()))
		{
			columnValues.putAll(row.columnValues);
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_ROW_UNKNOWN_TABLE, row.table.getName());
		}
	}

	public Set<String> getModifiedColumns()
	{
		return Collections.unmodifiableSet(modifiedColumns);
	}

	@Override
	public String toString()
	{
		return columnValues.toString();
	}
}
