package com.tlc.sql.internal.pgsql;

import com.tlc.sql.api.dml.OrderByClause;
import com.tlc.sql.internal.handler.AbstractDMLHandler;
import com.tlc.sql.api.meta.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Abishek
 * @version 1.0
 */

class PgsqlDMLHandler extends AbstractDMLHandler
{
	PgsqlDMLHandler()
	{
	}

	@Override
	public String getNextValueSequenceSQL(String sequenceName)
	{
		return "SELECT nextval('"+sequenceName+"')";
	}

	@Override
	public String getNextValueLimitSequenceSQL(String sequenceName)
	{
		return "SELECT nextval('"+sequenceName+"'), increment FROM information_schema.sequences where sequence_name='"+sequenceName+"'";
	}

	@Override
	protected String getBooleanString(Boolean value)
	{
		if (value)
		{
			return "true";
		}
		else
		{
			return "false";
		}
	}

	@Override
	protected String getOrderByClauseNullStr(OrderByClause.OrderType type, boolean nullFirst)
	{
		if(nullFirst)
		{
			return type == OrderByClause.OrderType.ASCENDING ? " NULLS FIRST": " NULLS LAST";
		}
		else
		{
			return type == OrderByClause.OrderType.ASCENDING ? " NULLS LAST" : " NULLS FIRST";
		}

	}

	@Override
	public Object getValueFromResultSet(ResultSet resultSet, int position, DataType dataType) throws SQLException
	{
		final Object object = resultSet.getObject(position);
		if (resultSet.wasNull())
		{
			return null;
		}
		else
		{
			return dataType.getWrappedValue(object);
		}
	}

}
