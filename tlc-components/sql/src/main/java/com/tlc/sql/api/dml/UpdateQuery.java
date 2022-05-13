package com.tlc.sql.api.dml;

import com.tlc.sql.internal.dml.UpdateQueryImpl;

import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public interface UpdateQuery extends Query, ActionQuery
{
	static UpdateQuery get(Table table)
	{
		return new UpdateQueryImpl(table);
	}

	void addUpdateColumn(Column column, Object value);

	Map<Column, Object> getColumnValueMap();
}
