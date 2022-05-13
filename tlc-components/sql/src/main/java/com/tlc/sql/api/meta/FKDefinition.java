package com.tlc.sql.api.meta;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class FKDefinition
{
	private final String name;
	private final String localTable;

	private final TableDefinition remoteTable;
	
    private final ColumnDefinition local;
	private final ColumnDefinition remote;
	
	private final FKConstraint constraint;

	public enum FKConstraint
	{
		ON_DELETE_RESTRICT, ON_DELETE_CASCADE, ON_DELETE_SET_NULL;

		public static FKConstraint get(String attribute)
		{
			if(attribute == null || attribute.isEmpty())
			{
				return null;
			}
			if(attribute.equalsIgnoreCase("ON-DELETE-CASCADE"))
			{
				return ON_DELETE_CASCADE;
			}
			if(attribute.equalsIgnoreCase("ON-DELETE-RESTRICT"))
			{
				return ON_DELETE_RESTRICT;
			}
			return null;
		}
	}
	
	private boolean biDirectional = false;
	
	public FKDefinition(String name, String localTable, TableDefinition remoteTable, ColumnDefinition local, ColumnDefinition remote, FKConstraint fkConstraint)
	{
		this.name = Objects.requireNonNull(name);
		this.localTable = Objects.requireNonNull(localTable);
		this.remoteTable = Objects.requireNonNull(remoteTable);
		this.local = Objects.requireNonNull(local);
		this.remote = Objects.requireNonNull(remote);
		this.constraint = Objects.requireNonNull(fkConstraint);
	}

	public String getName()
	{
		return name;
	}

	public ColumnDefinition getLocal()
	{
		return local;
	}

	public ColumnDefinition getRemote()
	{
		return remote;
	}
	
	public boolean isBiDirectional()
	{
		return biDirectional;
	}

	public void setBiDirectional(boolean biDirectional)
	{
		this.biDirectional = biDirectional;
	}

	public String getLocalTable()
	{
		return localTable;
	}

	public TableDefinition getReferenceTableDef()
	{
		return remoteTable;
	}

	public String getReferenceTable()
	{
		return remoteTable.getName();
	}

	public FKConstraint getConstraint()
	{
		return constraint;
	}

}
