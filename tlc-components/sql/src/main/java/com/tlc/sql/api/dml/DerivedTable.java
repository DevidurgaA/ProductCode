package com.tlc.sql.api.dml;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class DerivedTable extends Table
{
	private final SelectQuery selectQuery;
	private DerivedTable(String table, String alias, SelectQuery selectQuery)
	{
		super(table, alias);
		this.selectQuery = Objects.requireNonNull(selectQuery);
	}

	public static DerivedTable get(String table, String alias, SelectQuery selectQuery)
	{
		return new DerivedTable(table, alias, selectQuery);
	}

	public SelectQuery getSelectQuery()
	{
		return selectQuery;
	}
}
