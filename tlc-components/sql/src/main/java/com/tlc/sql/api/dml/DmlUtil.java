package com.tlc.sql.api.dml;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.meta.FKDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Abishek
 * @version 1.0
 */
public class DmlUtil
{
	public static List<JoinClause> getJoins(Table... tables)
	{
		if(tables == null || tables.length == 0)
		{
			return null;
		}
		else
		{
			final SortedSet<Table> tableSet = new TreeSet<>(Arrays.asList(tables));
			return getJoins(tableSet);
		}
	}

	public static SelectQuery getChildTableCheckQuery(Table parentTable, Set<Long> ids)
	{
		final String parentTableName = parentTable.getName();
		final SelectQuery selectQuery = SelectQuery.get(parentTable);
		final Collection<String> childTables = parentTable.getTableDefinition().getChildTables();
		for (String child : childTables)
		{
			final Table childTable = Table.get(child);
			selectQuery.addSelectClause(childTable.getPKColumn());
			selectQuery.addJoinClause(getJoinClauseFromTables(parentTableName, child, JoinClause.JoinType.LEFT));
		}
		selectQuery.setWhereClause(new WhereClause(Criteria.in(parentTable.getPKColumn(), ids)));
		return selectQuery;
	}

	public static List<JoinClause> getJoins(SortedSet<Table> tables)
	{
		final List<JoinClause> joinClauses = new ArrayList<>();
		final NavigableSet<JoinStatus> joinsStatusSet = new TreeSet<>();
		tables.forEach( table -> joinsStatusSet.add(new JoinStatus(table)));

		final JoinStatus baseTableStatus = joinsStatusSet.first(); //Base table
		baseTableStatus.isLinked = true;

		for (JoinStatus local : joinsStatusSet)
		{
			final Table localTable = local.table;
			for (JoinStatus remote : joinsStatusSet.tailSet(local, false))
			{
				if(remote.isLinked && local.isLinked )
				{
					continue;
				}
				final Table remoteTable = remote.table;
				if(local.isLinked)
				{
					final JoinClause joinClause = getJoinClauseFromTables(localTable, remoteTable, false);
					if(joinClause != null)
					{
						joinClauses.add(joinClause);
						remote.isLinked = true;
					}
				}
				else
				{
					final JoinClause joinClause = getJoinClauseFromTables(localTable, remoteTable, true);
					if(joinClause != null)
					{
						joinClauses.add(joinClause);
						local.isLinked = true;
					}
				}
			}
			if(!local.isLinked)
			{
				throw ErrorCode.get(SQLErrorCodes.DB_TABLE_RELATION_NOT_FOUND, "No relations found for table "+local.table.getName());
			}
		}
		return joinClauses;
	}

	public static JoinClause getJoinClauseFromTables(String local, String remote, JoinClause.JoinType joinType)
	{
		final Table localTable = Table.get(local);
		final Table remoteTable = Table.get(remote);
		return getJoinClauseFromTables(localTable, remoteTable, joinType);
	}

	public static JoinClause getJoinClauseFromTables(Table localTable, Table remoteTable, JoinClause.JoinType joinType)
	{
		if(localTable.getSeqId() < remoteTable.getSeqId())
		{
			for (Map.Entry<String, FKDefinition> fkDefinitionEntry : remoteTable.getTableDefinition().getFKDefinitions().entrySet())
			{
				final FKDefinition fkDefinition = fkDefinitionEntry.getValue();
				if(fkDefinition.getReferenceTable().equals(localTable.getName()))
				{
					final String localColumn = fkDefinition.getRemote().getColumnName();
					final String remoteColumn = fkDefinition.getLocal().getColumnName();
					return new JoinClause(localTable, localColumn, remoteTable, remoteColumn, joinType);
				}
			}
		}
		else
		{
			for (Map.Entry<String, FKDefinition> fkDefinitionEntry : localTable.getTableDefinition().getFKDefinitions().entrySet())
			{
				final FKDefinition fkDefinition = fkDefinitionEntry.getValue();
				if(fkDefinition.getReferenceTable().equals(remoteTable.getName()))
				{
					final String localColumn = fkDefinition.getLocal().getColumnName();
					final String remoteColumn = fkDefinition.getRemote().getColumnName();
					return new JoinClause(localTable, localColumn, remoteTable, remoteColumn, joinType);
				}
			}

		}
		throw ErrorCode.get(SQLErrorCodes.DB_TABLE_RELATION_NOT_FOUND, "No relations found for tables "+ localTable.getName() + " -> " + remoteTable.getName());
	}

	private static JoinClause getJoinClauseFromTables(Table localTable, Table remoteTable, boolean reverse)
	{
		final String localTableName = localTable.getName();
		for (Map.Entry<String, FKDefinition> fkDefinitionEntry : remoteTable.getTableDefinition().getFKDefinitions().entrySet())
		{
			final FKDefinition fkDefinition = fkDefinitionEntry.getValue();
			if(fkDefinition.getReferenceTable().equals(localTableName))
			{
				final String localColumn = fkDefinition.getRemote().getColumnName();
				final String remoteColumn = fkDefinition.getLocal().getColumnName();
				if(reverse)
				{
					return new JoinClause(remoteTable, remoteColumn, localTable, localColumn);
				}
				else
				{
					return new JoinClause(localTable, localColumn, remoteTable, remoteColumn);
				}
			}
		}
		return null;
	}

    public static Set<Table> findValidParentTables(Table table, Set<Table> availableTable)
    {
    	final Set<Table> validParentTable = new HashSet<>();
		final Set<String> tables = availableTable.stream().map(Table::getName).collect(Collectors.toSet());
		loadValidParentTables(table, tables, validParentTable);
		return validParentTable;
    }

	private static void loadValidParentTables(Table table, Set<String> tables, Set<Table> validParentTable)
	{
		final TableDefinition tableDefinition = table.getTableDefinition();
		final Collection<FKDefinition> fkDefinitions = tableDefinition.getFKDefinitions().values();
		for (FKDefinition fkDefinition : fkDefinitions)
		{
			final String remoteTable = fkDefinition.getReferenceTable();
			if(tables.remove(remoteTable) && !remoteTable.equals(fkDefinition.getLocalTable()))
			{
				final Table parent = Table.get(remoteTable);
				validParentTable.add(parent);
				loadValidParentTables(parent, tables, validParentTable);
			}
		}
	}

    private static class JoinStatus implements Comparable<JoinStatus>
	{
		private final Table table;
		private boolean isLinked;

		private JoinStatus(Table table)
		{
			this.table = table;
			this.isLinked = false;
		}

		@Override
		public int compareTo(JoinStatus remote)
		{
			final JoinStatus status = Objects.requireNonNull(remote);
			return table.compareTo(status.table);
		}
	}
}
