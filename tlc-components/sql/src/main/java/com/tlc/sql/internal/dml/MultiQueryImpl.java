package com.tlc.sql.internal.dml;

import com.tlc.sql.api.dml.JoinClause;
import com.tlc.sql.api.dml.MultiQuery;
import com.tlc.sql.api.dml.Table;

import java.util.*;


/**
 * @author Abishek
 * @version 1.0
 */
public abstract class MultiQueryImpl extends QueryImpl implements MultiQuery
{
	private final List<JoinClause> joins;
	private final Collection<Table> tables;

	MultiQueryImpl(Table table)
	{
		super(table);
		this.joins = new ArrayList<>();
		this.tables = new TreeSet<>();
		tables.add(table);
	}


	@Override
	public void addJoinClause(JoinClause join)
	{
		if (join != null)
		{
			tables.add(join.getRemoteTable());
			joins.add(join);
		}
	}

	@Override
	public void addJoinClause(List<JoinClause> joins)
	{
		if (joins != null && joins.size() > 0)
		{
			joins.forEach( join ->
			{
				tables.add(join.getRemoteTable());
				this.joins.add(join);
			});
		}
	}

	@Override
	public List<JoinClause> getJoinClause()
	{
		return Collections.unmodifiableList(joins);
	}

	@Override
	public boolean removeJoinClause(JoinClause join)
	{
		tables.remove(join.getRemoteTable());
		return joins.remove(join);
	}
		
	@Override
	public Collection<Table> getTables()
	{
		return Collections.unmodifiableCollection(tables);
	}
}
