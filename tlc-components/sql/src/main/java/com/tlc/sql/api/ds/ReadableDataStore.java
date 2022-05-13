package com.tlc.sql.api.ds;

import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.dml.ColumnReceiver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Abishek
 * @version 1.0
 */

public interface ReadableDataStore
{
	long get(CountQuery query);

	Row get(Table table, Long id);

	DataContainer get(Table table, Collection<Long> ids);

	DataContainer get(Table table, WhereClause whereClause);

	DataContainer get(Table table, WhereClause whereClause, Collection<Column> columns);

	DataContainer get(SelectQuery query);

	DataContainer get(Collection<String> tables, WhereClause whereClause);

	DataContainer get(Collection<String> tables, WhereClause whereClause, Collection<Column> columns);

	DataContainer get(SortedSet<Table> tables, WhereClause whereClause);

	DataContainer get(SortedSet<Table> tables, WhereClause whereClause, Collection<Column> columns);

	List<Map<String, Object>> getData(SelectQuery query);

	void fetchData(Table table, WhereClause whereClause, RowReceiver rowReceiver);

	void fetchData(Column column, WhereClause whereClause, ColumnReceiver rowReceiver);
}
