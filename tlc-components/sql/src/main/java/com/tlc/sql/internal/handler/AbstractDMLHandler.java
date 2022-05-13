package com.tlc.sql.internal.handler;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author Abishek
 * @version 1.0
 */
public abstract class AbstractDMLHandler implements DMLHandler
{
	private static final String PREPARED_STMT_CONSTANT = "?";

	@Override
	public String getSelectSQL(SelectQuery selectQuery)
	{
		final StringBuilder selectQueryBuilder = new StringBuilder(500);
		final SortedSet<Column> selectColumns = selectQuery.getSelectClause();
		if(selectColumns == null || selectColumns.isEmpty())
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_SELECT_COLUMNS);
		}

		selectQueryBuilder.append("SELECT ");
		fillSelectClause(selectQueryBuilder, selectColumns);

		final Table baseTable = selectQuery.getBaseTable();
		selectQueryBuilder.append(" FROM ");

		fillFromClause(selectQueryBuilder, baseTable, selectQuery.getJoinClause());

		final WhereClause whereClause = selectQuery.getWhereClause();
		if(whereClause != null)
		{
			selectQueryBuilder.append(" WHERE ");
			fillWhereClause(selectQueryBuilder, whereClause, true);
		}

		final List<GroupByClause> groupByClause = selectQuery.getGroupByClause();
		if(groupByClause != null && !groupByClause.isEmpty())
		{
			fillGroupByClause(selectQueryBuilder, groupByClause);
		}

		final List<OrderByClause> orderByClause = selectQuery.getOrderByClause();
		if(orderByClause != null && !orderByClause.isEmpty())
		{
			selectQueryBuilder.append(" ORDER BY ");
			fillOrderByClause(selectQueryBuilder, orderByClause);
		}
		else
		{
			final String defaultOrderBy = getDefaultOrderByClause(baseTable);
			if(defaultOrderBy != null)
			{
				selectQueryBuilder.append(" ORDER BY ").append(defaultOrderBy);
			}
		}

		final LimitClause limitClause = selectQuery.getLimitClause();
		if(limitClause != null)
		{
			selectQueryBuilder.append(" ").append(getLimitClauseStr(limitClause));
		}
        return selectQueryBuilder.toString();
	}

	@Override
	public String getCountQuery(CountQuery query)
	{
		final Table baseTable = query.getBaseTable();
		final StringBuilder selectQueryBuilder = new StringBuilder(500);
		selectQueryBuilder.append("SELECT COUNT(*) FROM ");
		fillFromClause(selectQueryBuilder, baseTable, query.getJoinClause());

		final WhereClause whereClause = query.getWhereClause();
		if(whereClause != null)
		{
			selectQueryBuilder.append(" WHERE ");
			fillWhereClause(selectQueryBuilder, whereClause, true);
		}
		return selectQueryBuilder.toString();
	}

	@Override
	public String getInsertPreparedStatementSQL(TableDefinition tableDef, Map<String, Object> columnValues)
	{
		final StringBuilder builder =new StringBuilder(400);
		builder.append("INSERT INTO ");
		builder.append(getTableName(tableDef)).append(" (");

		final Iterator<Entry<String, Object>> itr = columnValues.entrySet().iterator();
		final StringBuilder valuesBuffer = new StringBuilder(200);
		final int maxColumns = columnValues.size();
		for( int index = 1; itr.hasNext(); index++)
		{
			final Entry<String, Object> entry = itr.next();
			final Object value = entry.getValue();
			final String column = entry.getKey();
			final DataType dataType = tableDef.getColumnDefinition(column).getDataType();

			builder.append(getColumnName(column));

			if(value == PREPARED_STMT_CONSTANT)
			{
				valuesBuffer.append(PREPARED_STMT_CONSTANT);
			}
			else
			{
				valuesBuffer.append(getColumnValue(dataType, value));
			}
			if(index < maxColumns)
			{
				builder.append(", ");
				valuesBuffer.append(", ");
			}
		}
		builder.append(") VALUES (");
		builder.append(valuesBuffer);
		builder.append(")");
        return builder.toString();
	}

	@Override
	public String getInsertPreparedStatementSQL(TableDefinition tableDef)
	{
		final Map<String, Object> valueMap = new LinkedHashMap<>();
		final Collection<String> columns = tableDef.getColumns();
		columns.forEach( column -> valueMap.put(column , PREPARED_STMT_CONSTANT));
		return getInsertPreparedStatementSQL(tableDef, valueMap);
	}

	@Override
	public String getUpdatePreparedStatementSQL(TableDefinition tableDefinition)
	{
		final StringBuilder updateQueryBuilder = new StringBuilder(500);

		final String tableName = tableDefinition.getName();

		final Collection<String> columns = tableDefinition.getEditableColumns().keySet();
		final Table table = Table.get(tableName);
		final Column pkColumn = table.getPKColumn();

		final Map<Column, Object> columnValueMap = new LinkedHashMap<>();
		columns.forEach( column -> columnValueMap.put(table.getColumn(column) , PREPARED_STMT_CONSTANT));

		updateQueryBuilder.append("UPDATE ").append(getTableName(tableDefinition));
		updateQueryBuilder.append(" SET ");

		fillSetColumnValues(updateQueryBuilder, columnValueMap);

		final WhereClause criteriaBuilder = new WhereClause(Criteria.eq(pkColumn, PREPARED_STMT_CONSTANT));
		updateQueryBuilder.append(" WHERE ");
		fillWhereClause(updateQueryBuilder, criteriaBuilder, false);

		return updateQueryBuilder.toString();
	}

	@Override
	public String getUpdatePreparedStatementSQL(UpdateQuery updateQuery)
	{
		final StringBuilder updateQueryBuilder = new StringBuilder(500);
		final Table baseTable = updateQuery.getBaseTable();
		updateQueryBuilder.append("UPDATE ").append(getTableName(baseTable.getTableDefinition()));

		final Map<Column, Object> updateColumnMap = updateQuery.getColumnValueMap();
		if(updateColumnMap == null || updateColumnMap.isEmpty())
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_UPDATE_COLUMNS);
		}
		final Map<Column, Object> columnValueMap = new LinkedHashMap<>();
		updateColumnMap.keySet().forEach( column -> columnValueMap.put(column , PREPARED_STMT_CONSTANT));
		updateQueryBuilder.append(" SET ");

		fillSetColumnValues(updateQueryBuilder, columnValueMap);

		final WhereClause whereClause = updateQuery.getWhereClause();
		if(whereClause != null)
		{
			updateQueryBuilder.append(" WHERE ");
			fillWhereClause(updateQueryBuilder, whereClause, false);
		}
		return updateQueryBuilder.toString();
	}

	@Override
	public String getDeleteSQL(DeleteQuery deleteQuery)
	{
		final StringBuilder deleteQueryBuilder = new StringBuilder(100);
		deleteQueryBuilder.append("DELETE FROM ");
		final TableDefinition tableDef = deleteQuery.getBaseTable().getTableDefinition();
		deleteQueryBuilder.append(getTableName(tableDef));
		final WhereClause criteriaBuilder = deleteQuery.getWhereClause();
		if(criteriaBuilder != null)
		{
			deleteQueryBuilder.append(" WHERE ");
			fillWhereClause(deleteQueryBuilder, criteriaBuilder, false);
		}
		return deleteQueryBuilder.toString();
	}

	@Override
	public void updatePreparedStatement(PreparedStatement statement, TableDefinition tableDef, Row row) throws SQLException
	{
		final Map<String, ColumnDefinition> columns = tableDef.getEditableColumns();
		int index = 1;
		for (final Entry<String, ColumnDefinition> entry : columns.entrySet())
		{
			final String columnName = entry.getKey();
			final Object value = row.get(columnName);
			final DataType dataType = entry.getValue().getDataType();
			setInsidePreparedStatement(statement, index++, dataType, value);
		}
		// Where Clause
		final Long pkValue = row.getPKValue();
		setInsidePreparedStatement(statement, index, DataType.BIGINT, pkValue);
	}

	@Override
	public void updatePreparedStatement(PreparedStatement statement, UpdateQuery updateQuery) throws SQLException
	{
		final Map<Column , Object> dataMap = updateQuery.getColumnValueMap();
		int index = 1;
		for (final Entry<Column , Object> entry : dataMap.entrySet())
		{
			final Column column = entry.getKey();
			final Object value = entry.getValue();
			final DataType dataType = column.getColumnDefinition().getDataType();
			setInsidePreparedStatement(statement, index++, dataType, value);
		}
	}

	@Override
	public void insertPreparedStatement(PreparedStatement statement, TableDefinition tableDef, Row row) throws SQLException
	{
		final Map<String, ColumnDefinition> columns = tableDef.getColumnDefinition();
		int index = 1;
		for(Iterator<Entry<String, ColumnDefinition>> itr = columns.entrySet().iterator(); itr.hasNext(); index++)
		{
			final Entry<String, ColumnDefinition> entry = itr.next();
			final ColumnDefinition columnDef = entry.getValue();
			final DataType dataType = columnDef.getDataType();
			final Object value = row.get(entry.getKey());
			if(value == null)
			{
				setInsidePreparedStatement(statement, index, dataType, columnDef.getDefaultValue());
			}
			else
			{
				setInsidePreparedStatement(statement, index, dataType, value);
			}
		}
	}

	protected String getDefaultOrderByClause(Table baseTable)
	{
		return null;
	}

	protected String getLimitClauseStr(LimitClause limitClause)
	{
		final int start = limitClause.getStart();
		final int numberOfResults = limitClause.getNumberOfResults();

		if(start <= 0)
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_LIMIT_CLAUSE);
		}
		final StringBuilder limitBuilder = new StringBuilder(25);
		limitBuilder.append("LIMIT ");
		if(numberOfResults <= 0)
		{
			limitBuilder.append("ALL");
		}
		else
		{
			limitBuilder.append(numberOfResults);
		}
		if( start > 1 )
		{
			limitBuilder.append(" OFFSET ").append(start - 1);
		}
		return limitBuilder.toString();
	}

	protected String getFormattedString(Object value, DataType dataType)
	{
		return "'"+escapeSpecialChars(value.toString())+ "'";
	}

	protected String escapeSpecialChars(String value)
	{
		return value.replace("'","''").replace("\\", "\\\\").trim();
	}

	protected String getTableName(String table)
	{
		return DDLDMLUtil.getTableName(table);
	}

	protected String getTableName(TableDefinition tableDefinition)
	{
		return DDLDMLUtil.getTableName(tableDefinition);
	}

	protected String getColumnName(String column)
	{
		return DDLDMLUtil.getColumnName(column);
	}

	protected String getBitwiseFunction(Column column, boolean updateTableDef, Operator operator, Number value, Number response)
	{
		return "(" +getColumnIdentity(column, updateTableDef)+ (operator == Operator.BIT_AND ? " & " : " | ") + value+") = "+response;
	}

	protected String getColumnIdentity(Column column, boolean updateTableDef)
	{
		if(updateTableDef)
		{
			final Table table = column.getTable();
			if (table.hasAlias())
			{
				return getTableName(table.getAlias()) + (".") + getColumnName(column.getColumnName());
			}
			else
			{
				return getTableName(table.getTableDefinition()) + (".") + getColumnName(column.getColumnName());
			}
		}
		else
		{
			return getColumnName(column.getColumnName());
		}
	}

	private String getTableSource(Table table)
	{
		if(table instanceof final DerivedTable derivedTable)
		{
			final SelectQuery selectQuery = derivedTable.getSelectQuery();
			return "( " + getSelectSQL(selectQuery) + " ) AS " + getTableName(derivedTable.getAlias());
		}
		else
		{
			if(table.hasAlias())
			{
				return getTableName(table.getTableDefinition()) + " AS " + getTableName(table.getAlias());
			}
			else
			{
				return getTableName(table.getTableDefinition());
			}
		}
	}

	private void setInsidePreparedStatement(PreparedStatement statement, int index, DataType dataType, Object value) throws SQLException
	{
		final int sqlType = dataType.getSqlType();
		if(value == null)
		{
			statement.setNull(index, sqlType);
		}
		else if(sqlType == Types.BLOB)
		{
			if(value instanceof final byte[] bValue)
			{
				statement.setBytes(index, bValue);
			}
			else
			{
				statement.setBytes(index, value.toString().getBytes());
			}
		}
		else
		{
			statement.setObject(index, dataType.getUnWrappedValue(value));
		}

	}

	private void fillSetColumnValues(StringBuilder builder, Map<Column , Object> columnValueMap)
	{
		final Iterator<Entry<Column , Object>> entryItr =  columnValueMap.entrySet().iterator();
		int index = 1;
		final int size = columnValueMap.size();

		while(entryItr.hasNext())
		{
			final Entry<Column , Object> entry = entryItr.next();
			final Column column = entry.getKey();
			final Object value = entry.getValue();
			builder.append(getColumnName(column.getColumnName())).append("=");

			final DataType dataType = column.getColumnDefinition().getDataType();

			if(value == PREPARED_STMT_CONSTANT)
			{
				builder.append(PREPARED_STMT_CONSTANT);
			}
			else
			{
				builder.append(getColumnValue(dataType, value));
			}
			if(index++ < size)
			{
				builder.append(", ");
			}
		}
	}


	private Object getColumnValue(DataType dataType, Object value)
	{
		if(value == null)
		{
			return "NULL";
		}
		else
		{
			if(dataType.isNumeric())
			{
				return value;
			}
			else if(dataType == DataType.BOOLEAN)
			{
				return getBooleanString((Boolean) value);
			}
			else
			{
				return getFormattedString(value, dataType);
			}
		}
	}

	private void fillFromClause(StringBuilder builder, Table baseTable, List<JoinClause> joinClause)
	{
		builder.append(getTableSource(baseTable));
		if(joinClause != null)
		{
			for(JoinClause jClause : joinClause)
			{
				final JoinClause.JoinType joinType = jClause.getJoinType();
				if(joinType == JoinClause.JoinType.INNER)
				{
					builder.append(" INNER JOIN ");
				}
				else if(joinType == JoinClause.JoinType.LEFT)
				{
					builder.append(" LEFT JOIN ");
				}
				else
				{
					builder.append(" RIGHT JOIN ");
				}
				final Table remoteTable = jClause.getRemoteTable();
				builder.append(getTableSource(remoteTable)).append(" ON ");

				final StringJoiner stringJoiner = new StringJoiner(" AND ");
				final List<JoinClause.Relation> columns =  jClause.getRelations();
				for (JoinClause.Relation relation : columns)
				{
					final Column localColumn = relation.getLocalColumn();
					final Column remoteColumn = relation.getRemoteColumn();
					stringJoiner.add(getColumnIdentity(localColumn, true) + " = "+ getColumnIdentity(remoteColumn, true));
				}
				builder.append(stringJoiner);
			}
		}
	}

	private void fillSelectClause(StringBuilder builder, SortedSet<Column> selectOrder)
	{
		int index = 1;
		final int Size = selectOrder.size();
		for(Column column : selectOrder)
		{
			builder.append(getColumnStringForSelect(column));
			if(index++ < Size)
			{
				builder.append(", ");
			}
		}

	}

	private String getColumnStringForSelect(Column column)
	{
		final StringBuilder columnBuilder = new StringBuilder(50);
		final Column.ColumnType type =  column.getColumnType();
		switch (type) {
			case COUNT -> columnBuilder.append("COUNT(").append(getColumnIdentity(column, true)).append(")");
			case DISTINCT -> columnBuilder.append("DISTINCT(").append(getColumnIdentity(column, true)).append(")");
			case MAX -> columnBuilder.append("MAX(").append(getColumnIdentity(column, true)).append(")");
			case MIN -> columnBuilder.append("MIN(").append(getColumnIdentity(column, true)).append(")");
			case AVERAGE -> columnBuilder.append("AVERAGE(").append(getColumnIdentity(column, true)).append(")");
			case NORMAL -> columnBuilder.append(getColumnIdentity(column, true));
		}
		if(column.hasAlias())
		{
			columnBuilder.append(" AS ").append(getColumnName(column.getColumnAlias()));
		}
		return columnBuilder.toString();
	}

	private void fillOrderByClause(StringBuilder builder, Collection<OrderByClause> sortColumns)
	{
		final int size = sortColumns.size();
		int index = 1;
		for(OrderByClause orderBy : sortColumns )
		{
			final Column column = orderBy.getColumn();
			if(column.hasAlias())
			{
				builder.append(getColumnName(column.getColumnAlias()));
			}
			else
			{
				builder.append(getColumnIdentity(column, true));
			}
			if(orderBy.getType() == OrderByClause.OrderType.DESCENDING)
			{
				builder.append(" DESC");
			}
			final String nullSortOrder = getOrderByClauseNullStr(orderBy.getType(), orderBy.isNullFirst());
			if(nullSortOrder != null)
			{
				builder.append(getOrderByClauseNullStr(orderBy.getType(), orderBy.isNullFirst()));
			}
			if(index < size)
			{
				builder.append(",");
			}
			index++;
		}
	}

	private void fillGroupByClause(StringBuilder selectQueryBuilder, List<GroupByClause> groupByClause)
	{
		selectQueryBuilder.append(" GROUP BY ");
		final int size = groupByClause.size();
		int index = 1;
		for (GroupByClause clause : groupByClause)
		{
			final Column column = clause.getColumn();
			if(column.hasAlias())
			{
				selectQueryBuilder.append(getColumnName(column.getColumnAlias()));
			}
			else
			{
				selectQueryBuilder.append(getColumnIdentity(column, true));
			}
			if(index++ < size)
			{
				selectQueryBuilder.append(", ");
			}
		}
	}

	private void fillWhereClause(StringBuilder builder, WhereClause criteriaBuilder, boolean updateTableDef)
	{
		if(criteriaBuilder.isInverted())
		{
			throw ErrorCode.get(ErrorCodes.NOT_SUPPORTED);
		}

        if(criteriaBuilder.isConnector())
        {
			final WhereClause left = criteriaBuilder.getLeft();
			final WhereClause right = criteriaBuilder.getRight();
			builder.append(" ( ");
			fillWhereClause(builder, left, updateTableDef);
			builder.append(getWhereClauseConnectorString(criteriaBuilder.getConnector()));
			fillWhereClause(builder, right, updateTableDef);
			builder.append(" ) ");

        }
        else
        {
			final Criteria sqlCriteria = criteriaBuilder.getCriteria();
			final Column column = sqlCriteria.getColumn();
			final Object value = sqlCriteria.getValue();
			final Operator operator = sqlCriteria.getOperator();

			builder.append("(");
			if(value == null)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getNullComparator(operator));
			}
			else if(value == PREPARED_STMT_CONSTANT)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator)).append(PREPARED_STMT_CONSTANT);
			}
			else if(value instanceof final SelectQuery selectQuery)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator)).append("(").append(getSelectSQL(selectQuery)).append(")");
			}
			else
			{
				final DataType dataType = column.getColumnDefinition().getDataType();
				switch (dataType) {
					case CHAR, SCHAR, KCHAR -> loadStringComparatorValue(column, value, builder, operator, updateTableDef);
					case INTEGER, BIGINT, DECIMAL -> loadNumberComparatorValue(column, value, builder, operator, updateTableDef);
					case BOOLEAN -> loadBooleanComparator(column, value, builder, operator, updateTableDef);
					default -> throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
				}
			}
			builder.append(")");
        }
	}

	private void loadBooleanComparator(Column column, Object value, StringBuilder builder, Operator operator, boolean updateTableDef)
	{
		if(value instanceof final Boolean bool)
		{
			if(operator == Operator.EQUAL || operator == Operator.NOT_EQUAL)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator)).append(getBooleanString(bool));
			}
			else
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
		}
	}

	private void loadStringComparatorValue(Column column, Object value, StringBuilder builder, Operator operator, boolean updateTableDef)
	{
		builder.append(getColumnIdentity(column, updateTableDef));
		final DataType dataType = column.getColumnDefinition().getDataType();
		if(value instanceof final Iterable<?> collection)
		{
			final Iterator<?> itr = collection.iterator();
			if(!itr.hasNext())
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
			if(operator == Operator.IN || operator == Operator.NOT_IN)
			{
				builder.append(getComparator(operator));
				builder.append(" ( ");
				do
				{
					final Object cValue = itr.next();
					final Object converted = dataType.getUnWrappedValue(cValue);
					if(!(converted instanceof String))
					{
						throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
					}
					builder.append(getFormattedString(converted, dataType));
					if(itr.hasNext())
					{
						builder.append(",");
					}
					else
					{
						break;
					}
				}while (true);
				builder.append(" ) ");
			}
			else
			{
				throw ErrorCode.get(ErrorCodes.NOT_SUPPORTED);
			}
		}
		else
		{
			final Object converted = dataType.getUnWrappedValue(value);
			if(!(converted instanceof String))
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
			if(converted.equals(PREPARED_STMT_CONSTANT))
			{
				builder.append(getComparator(operator)).append(converted);
			}
			else if(operator == Operator.CONTAINS || operator == Operator.NOT_CONTAINS)
			{
				builder.append(getComparator(operator)).append(getFormattedString("%"+converted+"%", column.getColumnDefinition().getDataType()));
			}
			else if(operator == Operator.STARTS_WITH || operator == Operator.NOT_STARTS_WITH)
			{
				builder.append(getComparator(operator)).append(getFormattedString(converted+"%", column.getColumnDefinition().getDataType()));
			}
			else if(operator == Operator.EQUAL || operator == Operator.NOT_EQUAL)
			{
				builder.append(getComparator(operator)).append(getFormattedString(converted, column.getColumnDefinition().getDataType()));
			}
			else
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
		}
	}

	private void loadNumberComparatorValue(Column column, Object value, StringBuilder builder, Operator operator, boolean updateTableDef)
	{
		if(value instanceof final Iterable<?> collection)
		{
			final Iterator<?> itr = collection.iterator();
			if(!itr.hasNext())
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
			if(operator == Operator.IN || operator == Operator.NOT_IN)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator));
				builder.append(" ( ");
				do
				{
					final Object cValue = itr.next();
					if(!(cValue instanceof Number))
					{
						throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
					}
					builder.append(cValue);
					if(itr.hasNext())
					{
						builder.append(",");
					}
					else
					{
						break;
					}
				}while (true);
				builder.append(" ) ");
			}
			else
			{
				throw ErrorCode.get(ErrorCodes.NOT_SUPPORTED);
			}
		}
		else if(value instanceof final Number[] pair)
		{
			if(operator == Operator.BETWEEN || operator == Operator.NOT_BETWEEN)
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator)).append(getBetweenValueNumber(pair[0], pair[1]));
			}
			else if(operator == Operator.BIT_AND || operator == Operator.BIT_OR)
			{
				builder.append(getBitwiseFunction(column, updateTableDef, operator, pair[0], pair[1]));
			}
			else
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
		}
		else if(value instanceof Number)
		{
			if((operator == Operator.EQUAL) || (operator == Operator.NOT_EQUAL) || (operator == Operator.GREATER_EQUAL) ||
					(operator == Operator.GREATER_THAN) || (operator == Operator.LESS_THAN) || (operator == Operator.LESS_EQUAL))
			{
				builder.append(getColumnIdentity(column, updateTableDef)).append(getComparator(operator)).append(value);
			}
			else
			{
				throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
			}
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
		}
	}

	private String getWhereClauseConnectorString(WhereClause.Connector connector)
	{
		if(connector == WhereClause.Connector.AND)
		{
			return " AND ";
		}
		else
		{
			return " OR ";
		}
	}

	private String getComparator(Operator operator)
	{
		return switch (operator) {
			case EQUAL -> " = ";
			case NOT_EQUAL -> " != ";
			case CONTAINS, STARTS_WITH -> " LIKE ";
			case NOT_CONTAINS, NOT_STARTS_WITH -> " NOT LIKE ";
			case GREATER_EQUAL -> " >= ";
			case GREATER_THAN -> " > ";
			case LESS_EQUAL -> " <= ";
			case LESS_THAN -> " < ";
			case IN -> " IN ";
			case NOT_IN -> " NOT IN ";
			case BETWEEN -> " BETWEEN ";
			case NOT_BETWEEN -> " NOT BETWEEN ";
			default -> throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
		};
	}

	private String getBetweenValueNumber(Number start, Number end)
	{
		return start + " AND " + end;
	}

	private String getNullComparator(Operator operator)
	{
		if(operator == Operator.EQUAL)
		{
			return " IS NULL";
		}
		else if(operator == Operator.NOT_EQUAL)
		{
			return " IS NOT NULL";
		}
		else
		{
			throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COMPARATOR);
		}
	}

	protected abstract String getOrderByClauseNullStr(OrderByClause.OrderType type, boolean nullFirst);

	protected abstract String getBooleanString(Boolean value);
}
