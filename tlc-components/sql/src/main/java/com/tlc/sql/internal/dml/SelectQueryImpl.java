package com.tlc.sql.internal.dml;

import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.*;
import java.util.logging.Logger;


/**
 * @author Abishek
 * @version 1.0
 */
public class SelectQueryImpl extends MultiQueryImpl implements SelectQuery
{
	private LimitClause range = null;

	private final Map<Table, Column> pkSelectClause;
	private final List<OrderByClause> sortColumns;
	private final List<GroupByClause> groupByColumns;

	private final SortedSet<Column> selectedColumns;
	private final SortedSet<Table> selectedTable;

	protected static final Logger LOGGER = Logger.getLogger(SelectQueryImpl.class.getName());

	public SelectQueryImpl(Table baseTable)
	{
		super(baseTable);
		this.pkSelectClause = new HashMap<>();
		this.sortColumns = new ArrayList<>();
		this.groupByColumns = new ArrayList<>();

		this.selectedColumns = new TreeSet<>();
		this.selectedTable = new TreeSet<>();
	}

	@Override
	public void addSelectClause(Collection<Column> columns)
	{
		columns.forEach(this::addColumn);
	}

	@Override
	public void addSelectClause(Column column)
	{
		addColumn(column);
	}

	@Override
	public void addSelectClause(Table ... tables)
	{
		if(tables != null)
		{
			for(Table table : tables)
			{
				addSelectClause(table);
			}
		}
	}

	@Override
	public void addSelectClause(Table table)
	{
		final TableDefinition tableDef = table.getTableDefinition();
		tableDef.getColumns().stream().map(table::getColumn).forEach(this::addColumn);
	}

	private void addColumn(Column column)
	{
		final Table table = column.getTable();
		if(column.isPkColumn())
		{
			pkSelectClause.put(table, column);
		}
		selectedTable.add(table);
		selectedColumns.add(column);
	}

	@Override
	public void removeSelectClause(Column column)
	{
		selectedColumns.remove(column);
	}

	@Override
	public SortedSet<Column> getSelectClause()
	{
		return Collections.unmodifiableSortedSet(selectedColumns);
	}

	@Override
	public void addOrderByClause(OrderByClause column)
	{
		sortColumns.add(column);		
	}

	@Override
	public void addOrderByClause(List<OrderByClause> columns)
	{
		if (columns != null && columns.size() > 0)
		{
			sortColumns.addAll(columns);		
		}
	}

	@Override
	public boolean removeOrderByClause(OrderByClause column)
	{
		return sortColumns.remove(column);
	}

	@Override
	public List<OrderByClause> getOrderByClause()
	{
		return Collections.unmodifiableList(sortColumns);
	}

	@Override
	public void addGroupByClause(GroupByClause column)
	{
		groupByColumns.add(column);
	}

	@Override
	public void addGroupByClause(List<GroupByClause> columns)
	{
		if (columns != null && columns.size() > 0)
		{
			groupByColumns.addAll(columns);
		}
	}

	@Override
	public boolean removeGroupByClause(GroupByClause column)
	{
		return groupByColumns.remove(column);
	}

	@Override
	public List<GroupByClause> getGroupByClause()
	{
		return Collections.unmodifiableList(groupByColumns);
	}

	@Override
	public void setLimitClause(LimitClause limit)
	{
		this.range = limit;
	}

	@Override
	public LimitClause getLimitClause()
	{
		return range;
	}

	@Override
	public Map<Table, Column> getSelectPKInfo()
	{
		return Collections.unmodifiableMap(pkSelectClause);
	}

	@Override
	public void addPkIndexAndSelectClause()
	{
		if(selectedColumns.isEmpty())
		{
			final Collection<Table> allTables = getTables();
			for(Table table : allTables)
			{
				addSelectClause(table);
				final Column pkColumn = table.getPKColumn();
				pkSelectClause.put(table, pkColumn);
			}
		}
		else
		{
			for (Table table : selectedTable)
			{
				if(!pkSelectClause.containsKey(table))
				{
					final Column pkColumn = table.getPKColumn();
					selectedColumns.add(pkColumn);
					pkSelectClause.put(table, pkColumn);
				}
			}
		}
	}
}
