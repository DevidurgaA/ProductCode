package com.tlc.sql.api.dml;

import com.tlc.sql.internal.dml.SelectQueryImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;


/**
 * @author Abishek
 * @version 1.0
 */
public interface SelectQuery extends MultiQuery
{
	static SelectQuery get(Table table)
	{
		return new SelectQueryImpl(table);
	}

	void setLimitClause(LimitClause limit);
    
    LimitClause getLimitClause();
    
	void addSelectClause(Column column);

	void addSelectClause(Collection<Column> column);

	void addSelectClause(Table table);

	void addSelectClause(Table... table);

	void removeSelectClause(Column column);

	SortedSet<Column> getSelectClause();

	Map<Table, Column> getSelectPKInfo();

	void addPkIndexAndSelectClause();

	void addOrderByClause(OrderByClause column);

	void addOrderByClause(List<OrderByClause> columns);

	boolean removeOrderByClause(OrderByClause column);

	List<OrderByClause> getOrderByClause();

	void addGroupByClause(GroupByClause column);

	void addGroupByClause(List<GroupByClause> columns);
	
	boolean removeGroupByClause(GroupByClause column);
	
	List<GroupByClause> getGroupByClause();
}
