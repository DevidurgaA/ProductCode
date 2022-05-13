package com.tlc.sql.internal.listeners;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.ReadableDataStore;
import com.tlc.sql.api.listeners.RowDeleteListener;
import com.tlc.sql.api.listeners.RowIdDeleteListener;
import com.tlc.sql.api.meta.FKDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Abishek
 * @version 1.0
 */
public class DeleteActionProcessor
{
    private static final int DELETE_FETCH_LIMIT = 5_000;

    private static final int DELETE_FETCH_NONE = 0;
    private static final int DELETE_FETCH_PK = 1;
    private static final int DELETE_FETCH_FULL = 2;

    private final RowListenerContainer rowListenerContainer;
    private final ReadableDataStore readableDataStore;

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteActionProcessor.class);
    public DeleteActionProcessor(ReadableDataStore readableDataStore)
    {
        this.rowListenerContainer = RowListenerContainer.getInstance();
        this.readableDataStore = Objects.requireNonNull(readableDataStore);
    }

    public NavigableMap<Table, Map<Long, Row>> getPreDeleteSnapshot(Table table, WhereClause whereClause)
    {
        final long time = System.currentTimeMillis();
        final NavigableMap<Table, Map<Long, Row>> snapshot = new TreeMap<>();

        final int fetchType = getDeleteFetchType(table);
        if(fetchType == DELETE_FETCH_NONE)
        {
            return snapshot;
        }

        final Map<Long, Row> tableData = new HashMap<>();
        if(fetchType == DELETE_FETCH_PK)
        {
            final List<Long> pkIds = new ArrayList<>();
            if(loadPkFromCriteria(pkIds, table.getPKColumn(), whereClause))
            {
                pkIds.forEach(id -> tableData.put(id, null));
            }
            else
            {
                loadPreDeleteData(table, whereClause, true, tableData);
            }
        }
        else
        {
            loadPreDeleteData(table, whereClause, false, tableData);
        }
        if(tableData.size() > 0)
        {
            snapshot.put(table, tableData);
            table.getTableDefinition().getChildTables().forEach( childTableName -> loadSnapshotFromChildTable(table, Table.get(childTableName), tableData.keySet(), snapshot));
        }
        LOGGER.info("Time taken to prepare snapshot for table {} {}", table, System.currentTimeMillis() - time);
        return snapshot;
    }


    public void processDeleteAction(NavigableMap<Table, Map<Long, Row>> dataMap)
    {
        final long time = System.currentTimeMillis();
        dataMap.descendingMap().forEach( (table, map) ->
        {
            if(map.size() > 0)
            {
                final Collection<List<RowDeleteListener>> listenersList = rowListenerContainer.getDeleteListeners(table);
                if(listenersList != null)
                {
                    final long innerTime = System.currentTimeMillis();
                    LOGGER.debug("RowListener found for table {} to process delete action", table);
                    final Map<Long, Row> data = Collections.unmodifiableMap(map);
                    for (List<RowDeleteListener> listeners : listenersList)
                    {
                        for (RowDeleteListener listener : listeners)
                        {
                            try
                            {
                                if(data.size() == DELETE_FETCH_LIMIT)
                                {
                                    LOGGER.info("Delete fetch limit reached for table {}, trying to send notification to listener", table);
                                    if(listener.processOutOfRangeNotification())
                                    {
                                        LOGGER.info("Listener ready to process the request after limit handling, Table : {}", table);
                                        listener.deleteRows(data.values());
                                    }
                                    else
                                    {
                                        LOGGER.warn("Listener terminated the process after limit notification, Table : {}", table);
                                    }
                                }
                                else
                                {
                                    listener.deleteRows(data.values());
                                }
                            }
                            catch (Throwable throwable)
                            {
                                throw ErrorCode.getLite(SQLErrorCodes.DB_LISTENER_DELETE_ACTION_FAILED, throwable);
                            }
                        }
                    }
                    LOGGER.info("Time taken to complete listener delete call for table {} {}", table, System.currentTimeMillis() - innerTime);
                }

                final Collection<List<RowIdDeleteListener>> idListenersList = rowListenerContainer.getDeleteListeners_pk(table);
                if(idListenersList != null)
                {
                    final long innerTime = System.currentTimeMillis();
                    LOGGER.info("RowListener found for table {} to process delete action", table);
                    final Map<Long, Row> data = Collections.unmodifiableMap(map);
                    for (List<RowIdDeleteListener> listeners : idListenersList)
                    {
                        for (RowIdDeleteListener listener : listeners)
                        {
                            try
                            {
                                if(data.size() == DELETE_FETCH_LIMIT)
                                {
                                    LOGGER.info("Delete fetch limit reached for table {}, trying to send notification to listener", table);
                                    if(listener.processOutOfRangeNotification())
                                    {
                                        LOGGER.info("Listener ready to process the request after limit handling, Table : {}", table);
                                        listener.deleteRows(data.keySet());
                                    }
                                    else
                                    {
                                        LOGGER.warn("Listener terminated the process after limit notification, Table : {}", table);
                                    }
                                }
                                else
                                {
                                    listener.deleteRows(data.keySet());
                                }
                            }
                            catch (Throwable throwable)
                            {
                                throw ErrorCode.getLite(SQLErrorCodes.DB_LISTENER_DELETE_ACTION_FAILED, throwable);
                            }
                        }
                    }
                    LOGGER.info("Time taken to complete listener delete call for table {} {}", table, System.currentTimeMillis() - innerTime);
                }
            }
        });
        LOGGER.info("Time taken to process delete action for tables {} {}", dataMap.keySet(), System.currentTimeMillis() - time);
    }

    @SuppressWarnings("unchecked")
    private boolean loadPkFromCriteria(List<Long> pkIds, Column pkColumn, WhereClause criteriaBuilder)
    {
        if(criteriaBuilder.isConnector())
        {
            return loadPkFromCriteria(pkIds, pkColumn, criteriaBuilder.getRight()) && loadPkFromCriteria(pkIds, pkColumn, criteriaBuilder.getLeft());
        }
        else
        {
            final Criteria sqlCriteria = criteriaBuilder.getCriteria();
            final Column column = sqlCriteria.getColumn();
            if(column.equals(pkColumn))
            {
                final Object value = sqlCriteria.getValue();
                final Operator operator = sqlCriteria.getOperator();
                if(operator == Operator.IN)
                {
                    pkIds.addAll((Collection<Long>)value);
                    return true;
                }
                else if(operator == Operator.EQUAL)
                {
                    pkIds.add((Long)value);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }

    private void loadSnapshotFromChildTable(Table parentTable, Table table, Collection<Long> parentTableIds, NavigableMap<Table, Map<Long, Row>> snapshot)
    {
        final int fetchType = getDeleteFetchType(table, parentTable, parentTableIds);
        if(fetchType != DELETE_FETCH_NONE)
        {
            LOGGER.info("Child table {} found in nested delete call, parent table {}", table, parentTable);
            final String parentTableName = parentTable.getName();
            final TableDefinition tableDefinition = table.getTableDefinition();

            final Map<String, FKDefinition> fkDefinitionMap = tableDefinition.getFKDefinitions();
            final List<FKDefinition> linkedFK = fkDefinitionMap.values().stream()
                    .filter( availableFK -> availableFK.getConstraint() == FKDefinition.FKConstraint.ON_DELETE_CASCADE && availableFK.getReferenceTable().equals(parentTableName)).collect(Collectors.toList());
            final int size = linkedFK.size();
            if(size > 0)
            {
                if(size > 1)
                {
                    LOGGER.error("Multiple FK in from same parent table not supported {}", linkedFK);
                }
                else
                {
                    final FKDefinition fk = linkedFK.get(0);
                    final Column column = table.getColumn(fk.getLocal().getColumnName());
                    final WhereClause whereClause = new WhereClause(Criteria.in(column, parentTableIds));
                    final Map<Long, Row> tableData = new HashMap<>();
                    loadPreDeleteData(table, whereClause, fetchType == DELETE_FETCH_PK, tableData);
                    if(tableData.size() > 0)
                    {
                        snapshot.put(table, tableData);
                        table.getTableDefinition().getChildTables().forEach( childTableName -> loadSnapshotFromChildTable(table, Table.get(childTableName), tableData.keySet(), snapshot));
                    }
                }
            }
        }
    }

    private void loadPreDeleteData(Table table, WhereClause whereClause, boolean pkAlone, Map<Long, Row> tableData)
    {
        final SelectQuery query = SelectQuery.get(table);
        query.setLimitClause(new LimitClause(1, DELETE_FETCH_LIMIT));
        query.setWhereClause(whereClause);
        if(pkAlone)
        {
            query.addSelectClause(table.getPKColumn());
        }
        else
        {
            query.addSelectClause(table);
        }
        final DataContainer dataContainer = readableDataStore.get(query);
        dataContainer.getRows(table).forEach( row -> tableData.put(row.getPKValue(), row));
    }

    private int getDeleteFetchType(Table table)
    {
        if(rowListenerContainer.hasDeleteListeners(table))
        {
            return DELETE_FETCH_FULL;
        }
        else if(rowListenerContainer.hasDeleteListeners_pk(table))
        {
            return DELETE_FETCH_PK;
        }
        else
        {
            return DELETE_FETCH_NONE;
        }
    }

    private int getDeleteFetchType(Table childTable, Table parentTable, Collection<Long> parentPk)
    {
        final Collection<List<RowDeleteListener>> deleteListener = rowListenerContainer.getDeleteListeners(childTable);
        if(deleteListener != null && deleteListener.stream().anyMatch( listeners -> listeners.stream().anyMatch( listener -> !listener.ignoreParentDeleteAction(parentTable, parentPk))))
        {
            return DELETE_FETCH_FULL;
        }
        final Collection<List<RowIdDeleteListener>> idDeleteListener = rowListenerContainer.getDeleteListeners_pk(childTable);
        if(idDeleteListener != null && idDeleteListener.stream().anyMatch(listeners -> listeners.stream().anyMatch(listener -> !listener.ignoreParentDeleteAction(parentTable, parentPk))))
        {
            return DELETE_FETCH_PK;
        }
        return DELETE_FETCH_NONE;
    }
}
