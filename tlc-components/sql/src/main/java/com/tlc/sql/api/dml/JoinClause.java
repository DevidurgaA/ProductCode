package com.tlc.sql.api.dml;

import java.util.List;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class JoinClause
{
	public enum JoinType
	{
		LEFT, RIGHT, INNER
	}

	private final Table localTable;
	private final Table remoteTable;

	final List<Relation> relations;
	private final JoinType joinType;

	public JoinClause(Table localTable, String localColumn, Table remoteTable, String remoteColumn)
	{
		this(localTable.getColumn(localColumn), remoteTable.getColumn(remoteColumn), JoinType.INNER);
	}
	public JoinClause(Table localTable, String localColumn, Table remoteTable, String remoteColumn, JoinType joinType)
	{
		this(localTable, localTable.getColumn(localColumn), remoteTable, remoteTable.getColumn(remoteColumn), joinType);
	}

	public JoinClause(Column localColumn, Column remoteColumn)
	{
		this(localColumn, remoteColumn, JoinType.INNER);
	}

	public JoinClause(Column localColumn, Column remoteColumn, JoinType joinType)
	{
		this(localColumn.getTable(), remoteColumn.getTable(), List.of(new Relation(localColumn, remoteColumn)), joinType);
	}

	public JoinClause(Table localTable, Column localColumn, Table remoteTable, Column remoteColumn, JoinType joinType)
	{
		this(localTable, remoteTable, List.of(new Relation(localColumn, remoteColumn)), joinType);
	}


	public JoinClause(Table localTable, Table remoteTable, List<Relation> relations, JoinType joinType)
	{
		this.localTable = Objects.requireNonNull(localTable);
		this.remoteTable = Objects.requireNonNull(remoteTable);
		this.relations = List.copyOf(relations);
		this.joinType = Objects.requireNonNull(joinType);
	}

	public List<Relation> getRelations()
	{
		return relations;
	}

	public JoinType getJoinType()
	{
		return joinType;
	}

	public Table getLocalTable()
	{
		return localTable;
	}

	public Table getRemoteTable()
	{
		return remoteTable;
	}

	public static class Relation
	{
		private final Column localColumn;
		private final Column remoteColumn;

		public Relation(Column localColumn, Column remoteColumn)
		{
			this.localColumn = Objects.requireNonNull(localColumn);
			this.remoteColumn = Objects.requireNonNull(remoteColumn);
		}

		public Column getLocalColumn()
		{
			return localColumn;
		}

		public Column getRemoteColumn()
		{
			return remoteColumn;
		}
	}
}
