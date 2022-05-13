package com.tlc.sql.api;

import com.tlc.sql.api.dml.Column;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.internal.data.DataContainerImpl;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Stream;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DataContainer
{
	enum Operation
	{
		INSERT, UPDATE, DELETE
	}

	static DataContainer create()
	{
		return new DataContainerImpl();
	}

	boolean isEmpty();

	int size(Table table);

	void append(DataContainer dataContainer);

	Stream<Row> getRows(Table table);

	Set<Table> getTables();

	Map<Long, Row> getRowsMap(Table table);

	Row getRow(Table table);

	void updateRow(Row row);

	void storeRow(Row row);

	void addNewRow(Row row);

	void removeNewRow(Row row);

	void indexRows(Column column);

	Row getIndexedRow(Column column, Object value);

	Stream<Row> getIndexedRows(Column column, Object value);

	Stream<Row> getRows(Table table, WhereClause whereClause);

	Row getRow(Table table, Long id);

	Row getRow(Table table, WhereClause whereClause);

	void deleteRow(Row row);
	
	NavigableMap<Table, NavigableSet<Long>> getModifiedRows(Operation operation);

	Set<Long> clearActionRows(Operation operation, Table table); // handle with care

	void clearActionRows(Operation operation);  // handle with care
}
