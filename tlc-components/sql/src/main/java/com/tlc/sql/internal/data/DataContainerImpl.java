package com.tlc.sql.internal.data;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Column;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * @author Abishek
 * @version 1.0
 */
public class DataContainerImpl implements DataContainer
{
	private final Map<Table, Map<Long, Row>> rowContainer;
	private final Map<Table, Map<Column, Map<Object, Collection<Row>>>> indexedData;
	private final Map<Operation, NavigableMap<Table, NavigableSet<Long>>> modifiedRows;
	private static final Object NULL = new Object();

	public DataContainerImpl()
	{
		this.rowContainer = new HashMap<>();
		this.modifiedRows = new HashMap<>();
		this.indexedData = new HashMap<>();
	}

	public DataContainerImpl loadDataFromResultSet(ResultSet resultSet, Collection<Column> selectedColumns, Map<Table, Column> pkInfo, DataRetriever dataRetriever) throws SQLException
	{
		final Map<Table, ColumnMapping> tableColumnIndex = constructTableColumnIndex(pkInfo, selectedColumns);
		while(resultSet.next())
		{
			for (Entry<Table, ColumnMapping> entry : tableColumnIndex.entrySet())
			{
				final Table table = entry.getKey();
				final ColumnMapping mapping = entry.getValue();
				final Map<Long, Row> tableRows = rowContainer.computeIfAbsent(table, k -> new LinkedHashMap<>());
				try
				{
					final Long pkColumn = (Long) dataRetriever.getData(resultSet, mapping.pkColumnIndex, DataType.BIGINT);
					if (pkColumn == null)
					{
						continue;
					}
					if (!tableRows.containsKey(pkColumn))
					{
						final Row row = new Row(table, pkColumn);
						tableRows.put(pkColumn, row);
						if (!mapping.columnIndex.isEmpty())
						{
							for (Entry<Column, Integer> colEntry : mapping.columnIndex.entrySet())
							{
								final Column column = colEntry.getKey();
								final DataType dataType = column.getColumnDefinition().getDataType();
								final Integer index = colEntry.getValue();
								final Object value = dataRetriever.getData(resultSet, index, dataType);
								row.setWithoutCheck(column, value);
							}
						}
					}
				}
				catch (Exception exp)
				{
					throw ErrorCode.get(SQLErrorCodes.DB_DO_RESULTSET_READ_FAILED, exp);
				}
			}
		}
		return this;
	}

	@Override
	public void append(DataContainer dataContainer)
	{
		final DataContainerImpl dataContainerImpl = (DataContainerImpl) dataContainer;
		dataContainerImpl.rowContainer.forEach(( table, map) ->
				this.rowContainer.computeIfAbsent(table,  k -> new LinkedHashMap<>()).putAll(map));

		dataContainerImpl.modifiedRows.forEach( (operation, map) ->
		{
			final SortedMap<Table, NavigableSet<Long>> localMap = modifiedRows.computeIfAbsent(operation, k -> new TreeMap<>());
			map.forEach( (table, ids) ->
					localMap.computeIfAbsent(table, k -> new TreeSet<>()).addAll(ids));
		});
	}

	@Override
	public void indexRows(Column column)
	{
		final Table table = column.getTable();
		final Map<Long, Row> rows = rowContainer.get(table);
		if(rows != null)
		{
			final Map<Column, Map<Object, Collection<Row>>> tableIndex = indexedData.computeIfAbsent(table, k-> new HashMap<>());
			final Map<Object, Collection<Row>> columnIndex = new HashMap<>();
			for (Row row : rows.values())
			{
				columnIndex.computeIfAbsent(Objects.requireNonNullElse(row.get(column), NULL), k-> new LinkedList<>()).add(row);
			}
			tableIndex.put(column, columnIndex);
		}
	}

	@Override
	public Stream<Row> getIndexedRows(Column column, Object value)
	{
		final Map<Column, Map<Object, Collection<Row>>> tableIndex = indexedData.get(column.getTable());
		if(tableIndex != null)
		{
			final Map<Object, Collection<Row>> columnIndex = tableIndex.get(column);
			if(columnIndex != null)
			{
				final Collection<Row> columnValue = columnIndex.get(Objects.requireNonNullElse(value, NULL));
				if(columnValue != null)
				{
					return columnValue.stream();
				}
			}
		}
		return Stream.empty();
	}

	@Override
	public Row getIndexedRow(Column column, Object value)
	{
		return getIndexedRows(column, value).findFirst().orElse(null);
	}

	@Override
	public void storeRow(Row row)
	{
		final Table table = row.getTable();
		final Long pkColumn = row.getPKValue();
		final Map<Long, Row> tempData = rowContainer.computeIfAbsent(table, k -> new LinkedHashMap<>());
		tempData.put(pkColumn , new Row(row));
	}

	@Override
	public void addNewRow(Row row)
	{
		final Table table = row.getTable();
		final Long pkColumn = row.getPKValue();

		final SortedMap<Table, NavigableSet<Long>> tempAction = modifiedRows.computeIfAbsent(Operation.INSERT, k-> new TreeMap<>());
		final Set<Long> rowsCollection = tempAction.computeIfAbsent(table, k-> new TreeSet<>());
		rowsCollection.add(pkColumn);

		final Map<Long, Row> tempData = rowContainer.computeIfAbsent(table, k -> new LinkedHashMap<>());
		tempData.put(pkColumn , new Row(row));
	}

	@Override
	public void updateRow(Row row)
	{
		if(row.isNewRow())
		{
			addNewRow(row);
		}
		else
		{
			final Table table = row.getTable();
			final Long pkColumn = row.getPKValue();

			final Map<Table, NavigableSet<Long>> tempAction = modifiedRows.computeIfAbsent(Operation.UPDATE, k -> new TreeMap<>());
			final Collection<Long> rowsCollection = tempAction.computeIfAbsent(table, k -> new TreeSet<>());
			rowsCollection.add(pkColumn);

			final Map<Long, Row> tempData = rowContainer.computeIfAbsent(table, k -> new LinkedHashMap<>());
			tempData.put(pkColumn , new Row(row));
		}
	}

	@Override
	public void removeNewRow(Row row)
	{
		final Table table = row.getTable();
		final Long pkColumn = row.getPKValue();
		final SortedMap<Table, NavigableSet<Long>> tempAction = modifiedRows.computeIfAbsent(Operation.INSERT, k-> new TreeMap<>());
		final Collection<Long> rowsCollection = tempAction.computeIfAbsent(table, k-> new TreeSet<>());
		rowsCollection.remove(pkColumn);

		if(rowsCollection.isEmpty())
		{
			tempAction.remove(table);
			if(tempAction.isEmpty())
			{
				modifiedRows.remove(Operation.INSERT);
			}
		}

		final Map<Long, Row> tempData = rowContainer.computeIfAbsent(table, k -> new LinkedHashMap<>());
		tempData.remove(pkColumn);
		if(tempData.isEmpty())
		{
			rowContainer.remove(table);
		}
	}

	@Override
	public void deleteRow(Row row)
	{
		if(row.isNewRow())
		{
			removeNewRow(row);
		}
		else
		{
			final Table table = row.getTable();
			final Long pkColumn = row.getPKValue();

			final NavigableMap<Table, NavigableSet<Long>> tempAction = modifiedRows.computeIfAbsent(Operation.DELETE, k -> new TreeMap<>());
			final Collection<Long> rowsCollection = tempAction.computeIfAbsent(table, k-> new TreeSet<>());
			rowsCollection.add(pkColumn);
			rowContainer.computeIfAbsent(table, k-> new LinkedHashMap<>()).put(pkColumn , new Row(row));
		}
	}

	@Override
	public NavigableMap<Table, NavigableSet<Long>> getModifiedRows(Operation operation)
	{
		final NavigableMap<Table , NavigableSet<Long>> modified = modifiedRows.get(operation);
		if(modified == null)
		{
			return Collections.emptyNavigableMap();
		}
		final NavigableMap<Table, NavigableSet<Long>> newMap = new TreeMap<>();
		modified.forEach((key, value) -> newMap.put(key, Collections.unmodifiableNavigableSet(value)));
		return newMap;
	}

	@Override
	public Set<Long> clearActionRows(Operation operation, Table table)
	{
		final NavigableMap<Table, NavigableSet<Long>> tableData = modifiedRows.get(operation);
		if(tableData != null)
		{
			return tableData.remove(table);
		}
		else
		{
			return Set.of();
		}
	}

	@Override
	public void clearActionRows(Operation operation)
	{
		modifiedRows.remove(operation);
	}
	
	@Override
	public Stream<Row> getRows(Table table)
	{
		final Map<Long, Row> allRows = rowContainer.get(table);
		if(allRows == null)
		{
			return Stream.empty();
		}
		else
		{
			return allRows.values().stream();
		}
	}

	@Override
	public Set<Table> getTables()
	{
		return Collections.unmodifiableSet(rowContainer.keySet());
	}

	@Override
	public Map<Long, Row> getRowsMap(Table table)
	{
		final Map<Long, Row> allRows = rowContainer.get(table);
		if(allRows == null)
		{
			return Collections.emptyMap();
		}
		else
		{
			return Map.copyOf(allRows);
		}
	}

	@Override
	public Row getRow(Table table)
	{
		final Optional<Row> findFirst = getRows(table).findFirst();
		return findFirst.orElse(null);
	}

	@Override
	public Row getRow(Table table, Long id)
	{
		final Map<Long, Row> tableData = rowContainer.get(table);
		if(tableData != null)
		{
			return tableData.get(id);
		}
		else
		{
			return null;
		}
	}

	@Override
	public Stream<Row> getRows(Table table, WhereClause whereClause)
	{
		if(whereClause == null)
		{
			return getRows(table);
		}
		else
		{
			return getRows(table).filter(new DataContainerFilter(whereClause));
		}
	}

	@Override
	public Row getRow(Table table, WhereClause whereClause)
	{
		try
		{
			return getRows(table, whereClause).findFirst().orElse(null);
		}
		catch (NoSuchElementException exp)
		{
			return null;
		}
	}

	@Override
	public boolean isEmpty()
	{
		return rowContainer.isEmpty();
	}

	@Override
	public int size(Table table)
	{
		final Map<Long, Row> allRows = rowContainer.get(table);
		if(allRows == null)
		{
			return 0;
		}
		else
		{
			return allRows.size();
		}
	}
	
	
	private Map<Table , ColumnMapping> constructTableColumnIndex(Map<Table, Column> pkInfo, Collection<Column> selectedColumns)
	{
		final Map<Table, ColumnMapping> columnMappingMap = new HashMap<>();
		final AtomicInteger index = new AtomicInteger(1);
		selectedColumns.forEach( column ->
		{
			final Table table = column.getTable();
			final ColumnMapping columnMapping = columnMappingMap.computeIfAbsent(table, k-> new ColumnMapping());
			final Integer columnIndex = index.getAndIncrement();
			final Column pkColumn = pkInfo.get(table);
			if(pkColumn == column)
			{
				columnMapping.setPKColumnIndex(columnIndex);
			}
			else
			{
				columnMapping.addColumnIndex(column, columnIndex);
			}
		});
		return columnMappingMap;
	}

	@Override
	public String toString()
	{
		return rowContainer.toString();
	}
	
	private static class ColumnMapping
	{
		private final Map<Column, Integer> columnIndex;
		private Integer pkColumnIndex = null;
		
		ColumnMapping()
		{
			this.columnIndex = new HashMap<>();
		}
		
		void setPKColumnIndex(Integer index)
		{
			this.pkColumnIndex = index;
		}
		
		void addColumnIndex(Column column, Integer index)
		{
			columnIndex.put(column, index);
		}
	}
}
