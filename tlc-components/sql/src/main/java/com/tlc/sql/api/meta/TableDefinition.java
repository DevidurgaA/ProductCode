package com.tlc.sql.api.meta;

import java.util.*;

/**
 * @author Abishek
 * @version 1.0
 */
public class TableDefinition
{
	private final String name;
	private final String nameLowercase;

	private final PKDefinition pkDefinition;
	private final Map<String, ColumnDefinition> columnDefinitions;
	private final Map<String, FKDefinition> fkDefinitions;
    private final Map<String, UKDefinition> ukDefinitions;
	private final Map<String, IndexDefinition> indexDefinitions;
	private final Set<String> childTables;
	private final Map<String, ColumnDefinition> editableColumns;

	private final int seqId;
	private final TableType type;

	public TableDefinition(String name, TableType type, int seqId, TableMetaInput metaInput)
	{
		this.type = Objects.requireNonNull(type);
		this.seqId = seqId;
		this.pkDefinition = Objects.requireNonNull(metaInput.getPkDefinition());
		this.name = Objects.requireNonNull(name);
		this.nameLowercase = name.toLowerCase();

		this.fkDefinitions = Map.copyOf(metaInput.getFkDefinitions());
		this.ukDefinitions = Map.copyOf(metaInput.getUkDefinitions());
		this.columnDefinitions = Map.copyOf(metaInput.getColumnDefinitions());
		this.indexDefinitions = Map.copyOf(metaInput.getIndexDefinitions());
		this.childTables = Set.copyOf(metaInput.getChildTables());

		final Map<String, ColumnDefinition> editableColumns = new HashMap<>(columnDefinitions);
		editableColumns.remove(ImmutableColumns.ORG_ID.getName());
		if(pkDefinition.hasSequenceGenerator())
		{
			editableColumns.remove(pkDefinition.geColumnName());
		}
		this.editableColumns = Map.copyOf(editableColumns);
	}

	public String getName()
	{
		return name;
	}

	public String getLowercaseName()
	{
		return nameLowercase;
	}

	public TableType getType()
	{
		return type;
	}

	public Collection<String> getChildTables()
	{
		return childTables;
	}

	public Map<String, ColumnDefinition> getColumnDefinition()
	{
		return columnDefinitions;
	}

	public Map<String, IndexDefinition> getIndexDefinitions()
	{
		return indexDefinitions;
	}

	public ColumnDefinition getColumnDefinition(String columnName)
	{
		return columnDefinitions.get(columnName);
	}

	public Set<String> getColumns()
	{
		return columnDefinitions.keySet();
	}

	public Map<String, FKDefinition> getFKDefinitions()
	{
		return fkDefinitions;
	}

	public Map<String, UKDefinition> getUKDefinitions()
	{
		return ukDefinitions;
	}

	public PKDefinition getPkDefinition()
	{
		return pkDefinition;
	}

	public int getSeqId()
	{
		return seqId;
	}

	public Map<String, ColumnDefinition> getEditableColumns()
	{
		return editableColumns;
	}

	public Set<String> copyNonPkColumns()
	{
		final Set<String> columns = new HashSet<>(columnDefinitions.keySet());
		columns.remove(pkDefinition.geColumnName());
		return columns;
	}

}
