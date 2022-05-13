package com.tlc.sql.api.meta;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class PKDefinition
{
	private final String keyName;
	private final String columnName;
	private final SequenceDefinition sequenceDefinition;

	public PKDefinition(String keyName, String columnName)
	{
		this(keyName, columnName, null);
	}

	public PKDefinition(String keyName, String columnName, SequenceDefinition sequenceDefinition)
	{
		this.keyName = Objects.requireNonNull(keyName);
		this.columnName = Objects.requireNonNull(columnName);
		this.sequenceDefinition = sequenceDefinition;
	}
	
	public String geColumnName()
	{
		return columnName;
	}

	public String getKeyName()
	{
		return keyName;
	}

	public SequenceDefinition getSequenceDefinition()
	{
		return sequenceDefinition;
	}

	public long getNextSequence()
	{
		return sequenceDefinition.getSequenceGenerator().getNextNumber();
	}

	public boolean hasSequenceGenerator()
	{
		return sequenceDefinition != null;
	}
	@Override
	public String toString()
	{
		return keyName + "=" + columnName;
	}
}
