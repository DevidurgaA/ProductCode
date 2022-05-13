package com.tlc.sql.internal.ds;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.dml.DmlUtil;
import com.tlc.sql.api.ds.ReadableDataStore;
import com.tlc.sql.internal.data.DataContainerImpl;
import com.tlc.sql.internal.handler.DMLHandler;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @author Abishek
 * @version 1.0
 */

class ReadableDataStoreImpl implements ReadableDataStore
{
    private final DsProvider dsProvider;
    protected final DMLHandler dmlHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminDataStoreImpl.class);
    public ReadableDataStoreImpl(DsProvider dsProvider, DMLHandler dmlHandler)
    {
        this.dsProvider = Objects.requireNonNull(dsProvider);
        this.dmlHandler = Objects.requireNonNull(dmlHandler);
    }

    protected Connection getConnection()
    {
        try
        {
            return dsProvider.getConnection();
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_CONNECTION_FAILED, exp);
        }
    }

    @Override
    public Row get(Table table, Long id)
    {
        return get(table, new WhereClause(Criteria.eq(table.getPKColumn(), id))).getRow(table);
    }

    @Override
    public DataContainer get(Table table, Collection<Long> ids)
    {
        return get(table, new WhereClause(Criteria.in(table.getPKColumn(), ids)));
    }

    @Override
    public DataContainer get(Table table, WhereClause whereClause)
    {
        return get(table, whereClause, null);
    }

    @Override
    public DataContainer get(Table table, WhereClause whereClause, Collection<Column> columns)
    {
        final SelectQuery query = SelectQuery.get(table);
        if (columns != null && !columns.isEmpty())
        {
            query.addSelectClause(columns);
        }
        else
        {
            query.addSelectClause(table);
        }
        query.setWhereClause(whereClause);
        return get(query);
    }

    @Override
    public long get(CountQuery query)
    {
        final long time = System.currentTimeMillis();
        try (Connection conn = getConnection())
        {
            final long countResult = processCount(conn, query);
            LOGGER.debug("Time taken to execute execute countQuery {}", System.currentTimeMillis() - time);
            return countResult;
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, exp);
        }
    }

    @Override
    public DataContainer get(Collection<String> tables, WhereClause whereClause)
    {
        return get(tables, whereClause, null);
    }

    @Override
    public DataContainer get(Collection<String> tables, WhereClause whereClause, Collection<Column> columns)
    {
        final SortedSet<Table> sortedTables = new TreeSet<>();
        tables.forEach(tableStr -> sortedTables.add(Table.get(tableStr)));
        return get(sortedTables, whereClause, columns);
    }

    @Override
    public DataContainer get(SortedSet<Table> tables, WhereClause whereClause)
    {
        return get(tables, whereClause, null);
    }

    @Override
    public DataContainer get(SortedSet<Table> sortedTables, WhereClause whereClause, Collection<Column> columns)
    {
        if (sortedTables.size() == 1)
        {
            return get(sortedTables.iterator().next(), whereClause, columns);
        }
        else
        {
            final List<JoinClause> joins = DmlUtil.getJoins(sortedTables);
            if (joins.size() == 0)
            {
                throw ErrorCode.get(SQLErrorCodes.DB_TABLE_RELATION_NOT_FOUND);
            }
            else
            {
                final JoinClause firstJoin = joins.get(0);
                final SelectQuery sQuery = SelectQuery.get(firstJoin.getLocalTable());
                sQuery.setWhereClause(whereClause);
                if (columns != null && !columns.isEmpty())
                {
                    sQuery.addSelectClause(columns);
                }
                else
                {
                    sortedTables.forEach(sQuery::addSelectClause);
                }
                sQuery.addJoinClause(joins);
                return get(sQuery);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getData(SelectQuery query)
    {
        final long time = System.currentTimeMillis();
        try (Connection conn = getConnection())
        {
            final List<Map<String, Object>> result = processDirectSelect(conn, query);
            LOGGER.debug("Time taken to execute Direct SelectQuery {}", System.currentTimeMillis() - time);
            return result;
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, exp);
        }
    }

    @Override
    public void fetchData(Table table, WhereClause whereClause, RowReceiver rowReceiver)
    {
        final long time = System.currentTimeMillis();
        try (Connection conn = getConnection())
        {
            processDirectSelectListener(conn, table, whereClause, rowReceiver);
            LOGGER.debug("Time taken to execute Direct SelectQuery With listener {}", System.currentTimeMillis() - time);
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, exp);
        }
    }

    @Override
    public void fetchData(Column column, WhereClause whereClause, ColumnReceiver rowReceiver)
    {
        final long time = System.currentTimeMillis();
        try (Connection conn = getConnection())
        {
            processDirectSelectListener(conn, column, whereClause, rowReceiver);
            LOGGER.debug("Time taken to execute Direct SelectQuery With listener {}", System.currentTimeMillis() - time);
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, exp);
        }
    }

    @Override
    public DataContainer get(SelectQuery query)
    {
        final long time = System.currentTimeMillis();
        query.addPkIndexAndSelectClause();
        try (Connection conn = getConnection())
        {
            final DataContainer resultContainer = processSelect(conn, query);
            LOGGER.debug("Time taken to execute SelectQuery {}", System.currentTimeMillis() - time);
            return resultContainer;
        }
        catch (ErrorCode errorCode)
        {
            throw errorCode;
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, exp);
        }
    }

    private DataContainer processSelect(Connection connection, SelectQuery query)
    {
        final SelectQuery selectQuery = wrapSelectQuery(query);
        final String selectSql = dmlHandler.getSelectSQL(selectQuery);
        try(Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectSql))
        {
            final Collection<Column> selectedColumns = selectQuery.getSelectClause();
            final Map<Table, Column> pkInfo = selectQuery.getSelectPKInfo();
            return new DataContainerImpl().loadDataFromResultSet(rs, selectedColumns, pkInfo, dmlHandler::getValueFromResultSet);
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, selectSql, exp);
        }
    }

    private List<Map<String, Object>> processDirectSelect(Connection connection, SelectQuery query)
    {
        final SelectQuery selectQuery = wrapSelectQuery(query);
        final String selectSql = dmlHandler.getSelectSQL(selectQuery);
        try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(selectSql))
        {
            final List<Map<String, Object>> resultList = new LinkedList<>();
            final SortedSet<Column> selectClause = query.getSelectClause();
            while(resultSet.next())
            {
                final Map<String, Object> rowResult = new HashMap<>();
                int order = 1;
                for(Column column : selectClause)
                {
                    final DataType dataType = column.getColumnDefinition().getDataType();
                    final Object value = dmlHandler.getValueFromResultSet(resultSet, order++, dataType);
                    rowResult.put(column.getColumnAlias(), value);
                }
                resultList.add(rowResult);
            }
            return resultList;
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, selectSql, exp);
        }
    }

    private void processDirectSelectListener(Connection connection, Table table, WhereClause whereClause, RowReceiver rowReceiver)
    {
        final SelectQuery query = SelectQuery.get(table);
        query.addSelectClause(table);
        query.setWhereClause(whereClause);

        final SelectQuery selectQuery = wrapSelectQuery(query);
        final String selectSql = dmlHandler.getSelectSQL(selectQuery);
        try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(selectSql))
        {
            final SortedSet<Column> selectClause = query.getSelectClause();
            final Map<String, Object> rowResult = new HashMap<>();
            while(resultSet.next())
            {
                int order = 1;
                for(Column column : selectClause)
                {
                    final DataType dataType = column.getColumnDefinition().getDataType();
                    final Object value = dmlHandler.getValueFromResultSet(resultSet, order++, dataType);
                    rowResult.put(column.getColumnAlias(), value);
                }
                rowReceiver.process(rowResult);
                rowResult.clear();
            }
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, selectSql, exp);
        }
    }

    private void processDirectSelectListener(Connection connection, Column column, WhereClause whereClause, ColumnReceiver columnReceiver)
    {
        final Table table = column.getTable();

        final SelectQuery query = SelectQuery.get(table);
        query.addSelectClause(column);
        query.setWhereClause(whereClause);

        final SelectQuery selectQuery = wrapSelectQuery(query);
        final String selectSql = dmlHandler.getSelectSQL(selectQuery);
        try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(selectSql))
        {
            while(resultSet.next())
            {
                final DataType dataType = column.getColumnDefinition().getDataType();
                final Object value = dmlHandler.getValueFromResultSet(resultSet, 1, dataType);
                columnReceiver.process(value);
            }
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, selectSql, exp);
        }
    }


    private long processCount(Connection connection, CountQuery query)
    {
        final CountQuery countQuery = wrapCountQuery(query);
        final String selectSql = dmlHandler.getCountQuery(countQuery);
        try(Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectSql))
        {
            final long countResult;
            if(rs.next())
            {
                final Object result = rs.getObject(1);
                if(result instanceof Long)
                {
                    countResult = (Long) result;
                }
                else if (result instanceof Integer)
                {
                    countResult = ((Integer) result).longValue();
                }
                else
                {
                    countResult = Long.parseLong(result.toString());
                }
            }
            else
            {
                countResult = 0;
            }
            return countResult;
        }
        catch(Exception exp)
        {
            throw ErrorCode.get(SQLErrorCodes.DB_TABLE_SELECT_FAILED, selectSql, exp);
        }
    }

    protected SelectQuery wrapSelectQuery(SelectQuery selectQuery)
    {
        return selectQuery;
    }

    protected CountQuery wrapCountQuery(CountQuery countQuery)
    {
        return countQuery;
    }
}
