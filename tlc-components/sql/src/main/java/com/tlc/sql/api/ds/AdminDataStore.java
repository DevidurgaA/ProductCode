package com.tlc.sql.api.ds;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.update.ddl.DDLAction;
import com.tlc.sql.api.meta.TableDefinition;
import org.javatuples.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;


/**
 * @author Abishek
 * @version 1.0
 */
public interface AdminDataStore extends WritableDataStore
{
	Connection getConnection() throws SQLException;

	void initializeSequence(String sequenceName, int start, int incrementBy);

	void createSequence(String sequenceName, int start, int incrementBy);

	void updateSequence(String sequenceName, int incrementBy);

	long getSequenceNextValue(String sequenceName);

	Pair<Long, Long> getSequenceNextAndLimitValue(String sequenceName);

	boolean isTableExists(String tableName) throws SQLException;

	void createTables(Collection<TableDefinition> tableDefinitions);

	void executeDDLActions(List<? extends DDLAction> actions);

	void revertDDLActions(List<? extends AdvDDLAction> actions);
}
