package com.tlc.sql.internal.dml;

import com.tlc.sql.api.dml.Query;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class QueryImpl implements Query
{
	private final Table baseTable;
	private WhereClause whereClause = null;

	QueryImpl(Table table)
	{
		this.baseTable = Objects.requireNonNull(table);
	}
	
	@Override
	public void setWhereClause(WhereClause whereClause)
	{
		this.whereClause = whereClause;
	}

	@Override
	public WhereClause getWhereClause()
	{
		return whereClause;
	}

	@Override
	public Table getBaseTable()
	{
		return baseTable;
	}
}
