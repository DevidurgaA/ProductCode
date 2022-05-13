package com.tlc.sql.internal.pgsql;

import com.tlc.sql.internal.handler.AbstractDDLHandler;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;

/**
 * @author Abishek
 * @version 1.0
 */
class PgsqlDDLHandler extends AbstractDDLHandler
{
	private static final String LENGTH_CHECK_CONSTRAINT_NAME_FORMAT = "%s_LENGTH_AUTO";
	PgsqlDDLHandler()
	{
	}

	@Override
	protected String getColumnSQL(ColumnDefinition definition)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(getColumnName(definition));
		builder.append(" ");
		builder.append(getDataType(definition, definition.getMaxLength()));
		builder.append(definition.isNullable() ? " NULL " : " NOT NULL ");

		final DataType dataType = definition.getDataType();
		if(dataType == DataType.CHAR || dataType == DataType.SCHAR || dataType == DataType.KCHAR)
		{
			builder.append(" ").append(getLengthCheckConstraint(definition, String.format(LENGTH_CHECK_CONSTRAINT_NAME_FORMAT, definition.getColumnName())));
		}
		return builder.toString();
	}

	@Override
	public String[] getUpdateColumnLengthSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
	{
		final String constraintName = String.format(LENGTH_CHECK_CONSTRAINT_NAME_FORMAT, columnDefinition.getColumnName());
		final String tableName = getTableName(tableDefinition);
		return new String[] { "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName,
				"ALTER TABLE " + tableName + " ADD " + getLengthCheckConstraint(columnDefinition, constraintName)};
	}

	@Override
	public String getChangeColumnNotNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
	{
		return "ALTER TABLE " + getTableName(tableDefinition) + " ALTER COLUMN " + getColumnName(columnDefinition)
				+ " SET NOT NULL";
	}

	@Override
	public String getChangeColumnNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
	{
		return "ALTER TABLE " + getTableName(tableDefinition) + " ALTER COLUMN " + getColumnName(columnDefinition)
				+ " DROP NOT NULL";
	}

	@Override
	public String[] getChangeColumnDataTypeSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
	{
		final String tableName = getTableName(tableDefinition);
		final DataType dataType = columnDefinition.getDataType();
		if(dataType == DataType.STEXT || dataType == DataType.TEXT)
		{
			final String constraintName = String.format(LENGTH_CHECK_CONSTRAINT_NAME_FORMAT, columnDefinition.getColumnName());
			return new String[] { "ALTER TABLE " + tableName + " DROP CONSTRAINT IF EXISTS "+ constraintName,
					" ALTER TABLE " + tableName + " ALTER COLUMN " + getColumnName(columnDefinition)+ " TYPE " + getDataType(columnDefinition, columnDefinition.getMaxLength())};
		}
		else if(dataType == DataType.CHAR || dataType == DataType.SCHAR || dataType == DataType.KCHAR)
		{
			final String constraintName = String.format(LENGTH_CHECK_CONSTRAINT_NAME_FORMAT, columnDefinition.getColumnName());
			return new String[] { "ALTER TABLE " + tableName + " ALTER COLUMN " + getColumnName(columnDefinition)+ " TYPE " + getDataType(columnDefinition, columnDefinition.getMaxLength()),
					"ALTER TABLE "+ tableName +" ADD "+getLengthCheckConstraint(columnDefinition, constraintName)};
		}
		else
		{
			return new String[] { "ALTER TABLE " + tableName +" ALTER COLUMN " + getColumnName(columnDefinition)+ " TYPE " + getDataType(columnDefinition, columnDefinition.getMaxLength())};
		}
	}

	@Override
	protected String getDataType(ColumnDefinition definition, int maxLength)
	{
		final DataType dataType = definition.getDataType();

		return switch (dataType) {
			case SCHAR, STEXT, KCHAR -> "TEXT";
			case TEXT, CHAR -> "CITEXT";
			case SMALLINT -> "SMALLINT";
			case INTEGER -> "INT";
			case BIGINT -> "BIGINT";
			case DECIMAL -> "DECIMAL(" + definition.getPrecision() + "," + definition.getScale() + ")";
			case BOOLEAN -> "BOOLEAN";
			case BLOB -> "BYTEA";
		};
	}

	private String getLengthCheckConstraint(ColumnDefinition definition, String name)
	{
		return "CONSTRAINT " + name +
				" CHECK ( LENGTH (" +
				getColumnName(definition) +
				") <= " +
				definition.getMaxLength() +
				")";
	}

}
