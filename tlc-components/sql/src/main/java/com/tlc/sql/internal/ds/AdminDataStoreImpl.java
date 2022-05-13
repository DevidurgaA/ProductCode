package com.tlc.sql.internal.ds;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.update.ddl.DDLAction;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.internal.handler.DMLHandler;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class AdminDataStoreImpl extends WritableDataStoreImpl implements AdminDataStore
{
    private final DDLHandler ddlHandler;
    private final DMLHandler dmlHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminDataStoreImpl.class);
    public AdminDataStoreImpl(DsProvider dsProvider, DMLHandler dmlHandler, DDLHandler ddlHandler)
    {
        super(dsProvider, dmlHandler);
        this.dmlHandler = Objects.requireNonNull(dmlHandler);
        this.ddlHandler = Objects.requireNonNull(ddlHandler);
    }

    @Override
    public Connection getConnection()
    {
        return super.getConnection();
    }

    @Override
    public void initializeSequence(String sequenceName, int start, int incrementBy)
    {
        final String sql = ddlHandler.getInitializeSequenceSQL(sequenceName, start, incrementBy);
        try (Connection conn = getConnection(); Statement statement = conn.createStatement())
        {
            statement.execute(sql);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_INITIALIZE_FAILED, exp);
        }
    }

    @Override
    public void createSequence(String sequenceName, int start, int incrementBy)
    {
        final String sql = ddlHandler.getCreateSequenceSQL(sequenceName, start, incrementBy);
        try (Connection conn = getConnection(); Statement statement = conn.createStatement())
        {
            statement.execute(sql);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_CREATE_FAILED, exp);
        }
    }

    @Override
    public void updateSequence(String sequenceName, int incrementBy)
    {
        final String sql = ddlHandler.getUpdateSequenceSQL(sequenceName, incrementBy);
        try (Connection conn = getConnection(); Statement statement = conn.createStatement())
        {
            statement.execute(sql);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_UPDATE_FAILED, exp);
        }
    }

    @Override
    public long getSequenceNextValue(String sequenceName)
    {
        final String sql = dmlHandler.getNextValueSequenceSQL(sequenceName);
        try (Connection conn = getConnection(); Statement statement = conn.createStatement(); ResultSet resultSet = statement.executeQuery(sql))
        {
            resultSet.next();
            return resultSet.getLong(1);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_LOOKUP_FAILED, exp);
        }
    }

    @Override
    public Pair<Long, Long> getSequenceNextAndLimitValue(String sequenceName)
    {
        final String sql = dmlHandler.getNextValueLimitSequenceSQL(sequenceName);
        try (Connection conn = getConnection(); Statement statement = conn.createStatement(); ResultSet resultSet = statement.executeQuery(sql))
        {
            if(resultSet.next())
            {
                final Long nextValue = resultSet.getLong(1);
                final Long limit = resultSet.getLong(1);
                return Pair.with(nextValue, limit);
            }
            else
            {
                throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_LOOKUP_FAILED);
            }
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_SEQUENCE_LOOKUP_FAILED, exp);
        }
    }

    @Override
    public boolean isTableExists(String tableName) throws SQLException
    {
        try (Connection conn = getConnection())
        {
            return isTableExists(conn, tableName);
        }
    }

    @Override
    public void createTables(Collection<TableDefinition> tableDefinitions)
    {
        try (Connection conn = getConnection())
        {
            conn.setAutoCommit(false);
            try
            {
                createTable(ddlHandler, conn, tableDefinitions);
                conn.commit();
            }
            catch (Throwable exp)
            {
                conn.rollback();
                throw exp;
            }
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_CREATE_TABLE_FAILED, exp);
        }
    }

    @Override
    public void executeDDLActions(List<? extends DDLAction> actions)
    {
        final long time = System.currentTimeMillis();
        try (Connection connection = getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                executeDDLActions(ddlHandler, connection, actions);
                connection.commit();
            }
            catch (Throwable exp)
            {
                connection.rollback();
                throw exp;
            }
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_DDL_ACTION_FAILED, exp);
        }
        LOGGER.debug("Time taken to process DDL Actions {}", System.currentTimeMillis() - time);
    }

    @Override
    public void revertDDLActions(List<? extends AdvDDLAction> actions)
    {
        final long time = System.currentTimeMillis();
        try (Connection connection = getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                revertDDLActions(ddlHandler, connection, actions);
                connection.commit();
            }
            catch (Throwable exp)
            {
                connection.rollback();
                throw exp;
            }
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_DDL_ACTION_FAILED, exp);
        }
        LOGGER.debug("Time taken to Revert DDL Actions {}", System.currentTimeMillis() - time);
    }

    private boolean isTableExists(Connection connection, String tableName) throws SQLException
    {
        try(ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null))
        {
            if(rs.next())
            {
                return true;
            }
        }
        try(ResultSet rs = connection.getMetaData().getTables(null, null, tableName.toLowerCase(), null))
        {
            if(rs.next())
            {
                return true;
            }
        }
        try(ResultSet rs = connection.getMetaData().getTables(null, null, tableName.toUpperCase(), null))
        {
            return rs.next();
        }
    }

    private void createTable(DDLHandler ddlHandler, Connection connection, Collection<TableDefinition> tableDefinitions)
    {
        try(Statement statement = connection.createStatement())
        {
            for (TableDefinition tableDefinition : tableDefinitions)
            {
                final String[] createSql = ddlHandler.getCreateTableSQL(tableDefinition);
                for (String sql : createSql)
                {
                    statement.addBatch(sql);
                }
            }
            statement.executeBatch();
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_CREATE_TABLE_FAILED, exp);
        }
    }

    private void executeDDLActions(DDLHandler ddlHandler, Connection connection, List<? extends DDLAction> ddlActions)
    {
        try
        {
            try(Statement statement = connection.createStatement())
            {
                for (DDLAction ddlAction : ddlActions)
                {
                    final String[] sqlList = ddlAction.getSQL(ddlHandler);
                    for (String sql : sqlList)
                    {
                        LOGGER.info("Going to execute - {}", sql);
                        statement.execute(sql);
                        LOGGER.info("Successfully executed - {}", sql);
                    }
                }
            }
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_DDL_ACTION_FAILED, exp);
        }
    }

    private void revertDDLActions(DDLHandler ddlHandler, Connection connection, List<? extends AdvDDLAction> actions)
    {
        try
        {
            try(Statement statement = connection.createStatement())
            {
                for (AdvDDLAction ddlAction : actions)
                {
                    final String[] sqlList = ddlAction.getRevertSQL(ddlHandler);
                    for (String sql : sqlList)
                    {
                        LOGGER.info("Going to execute - {}", sql);
                        statement.execute(sql);
                        LOGGER.info("Successfully executed - {}", sql);
                    }
                }
            }
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_DDL_ACTION_FAILED, exp);
        }
    }
}
