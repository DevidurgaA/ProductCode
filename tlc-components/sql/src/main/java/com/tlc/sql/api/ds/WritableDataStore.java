package com.tlc.sql.api.ds;

import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;

import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;


/**
 * @author Abishek
 * @version 1.0
 */
public interface WritableDataStore extends ReadableDataStore
{
	void update(UpdateQuery query);

	void update(List<UpdateQuery> queries);

	void delete(Table table, WhereClause whereClause);

	void delete(Table table, Long id);

	void delete(Table table, Collection<Long> ids);

	void delete(DeleteQuery deleteQuery);

	void delete(List<DeleteQuery> deleteQueries);

	void delete(NavigableMap<Table, NavigableSet<Long>> tableAndPk);

	void addRow(Row row);

	void addRows(List<Row> rows);

	void executeActions(List<ActionQuery> queries);

	void commitChanges(DataContainer dataContainer);
}
