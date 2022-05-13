package com.tlc.sql.internal.handler;

import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.CountQuery;
import com.tlc.sql.api.dml.DeleteQuery;
import com.tlc.sql.api.dml.SelectQuery;
import com.tlc.sql.api.dml.UpdateQuery;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.api.meta.TableDefinition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DMLHandler
{
	String getNextValueSequenceSQL(String sequenceName);

	String getNextValueLimitSequenceSQL(String sequenceName);

	String getSelectSQL(SelectQuery query);

	String getCountQuery(CountQuery query);

	String getDeleteSQL(DeleteQuery deleteQuery);

	String getInsertPreparedStatementSQL(TableDefinition tableDef, Map<String, Object> columnValues);
	
	String getInsertPreparedStatementSQL(TableDefinition tableDef);

	String getUpdatePreparedStatementSQL(UpdateQuery selectQuery);

	String getUpdatePreparedStatementSQL(TableDefinition tableDef);

	void updatePreparedStatement(PreparedStatement statement, UpdateQuery updateQuery) throws SQLException;

	void updatePreparedStatement(PreparedStatement statement, TableDefinition tableDef, Row row) throws SQLException;

	void insertPreparedStatement(PreparedStatement statement, TableDefinition tableDef, Row row) throws SQLException;

	Object getValueFromResultSet(ResultSet resultSet, int position, DataType dataType) throws SQLException;
}
