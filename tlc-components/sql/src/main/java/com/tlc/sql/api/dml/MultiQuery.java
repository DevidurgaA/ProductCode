package com.tlc.sql.api.dml;

import java.util.Collection;
import java.util.List;


/**
 * @author Abishek
 * @version 1.0
 */
public interface MultiQuery extends Query
{
	void addJoinClause(JoinClause join);
	
	void addJoinClause(List<JoinClause> joins);
	
	List<JoinClause> getJoinClause();

	boolean removeJoinClause(JoinClause join);
	
	Collection<Table> getTables();
}
