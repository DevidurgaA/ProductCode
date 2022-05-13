package com.tlc.sql.internal.listeners;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.ReadableDataStore;
import com.tlc.sql.api.listeners.RowUpdateListener;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Abishek
 * @version 1.0
 */
public class UpdateActionProcessor
{
    private static final int UPDATE_FETCH_LIMIT = 5_000;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateActionProcessor.class);
    private final RowListenerContainer rowListenerContainer;
    private final ReadableDataStore readableDataStore;

    public UpdateActionProcessor(ReadableDataStore readableDataStore)
    {
        this.readableDataStore = Objects.requireNonNull(readableDataStore);
        this.rowListenerContainer = RowListenerContainer.getInstance();
    }

    public void processPostUpdateAction(SortedMap<Table, Map<Long, Row>> dataMap)
    {
        final long time = System.currentTimeMillis();
        final Set<Table> tables = Collections.unmodifiableSet(dataMap.keySet());
        dataMap.forEach( (table, rowInfo) ->
        {
            if(!rowInfo.isEmpty())
            {
                final Collection<List<RowUpdateListener>> listenersList = rowListenerContainer.getUpdateListeners(table);
                if(listenersList != null)
                {
                    final Map<Long, Row> correctedData = new HashMap<>();
                    LOGGER.debug("RowListener found for table {} to process add action", table);

                    final Collection<Long> inCompleteData = new ArrayList<>();
                    rowInfo.forEach( (pk, row) ->
                    {
                        final Set<String> modifiedColumns = row.getModifiedColumns();
                        if(!modifiedColumns.isEmpty() && listenersList.stream().anyMatch( list -> list.stream().anyMatch( listener ->
                                !listener.ignore(tables) && !listener.ignoreUpdate(table, modifiedColumns))))
                        {
                            if(row.isCompleteRow())
                            {
                                correctedData.put(pk, row);
                            }
                            else
                            {
                                inCompleteData.add(pk);
                            }
                        }
                    });
                    final int size = inCompleteData.size();
                    if(size > 0)
                    {
                        LOGGER.info("Fetching incomplete data from table {} to process update action, count {}", table, size);
                        final WhereClause whereClause = new WhereClause(Criteria.in(table.getPKColumn(), inCompleteData));
                        final DataContainer dataContainer = readableDataStore.get(table, whereClause);
                        dataContainer.getRows(table).forEach( row ->
                        {
                            final Long pkValue = row.getPKValue();
                            final Row existing = rowInfo.get(pkValue);
                            existing.merge(row);
                            correctedData.put(pkValue, existing);
                        });
                    }
                    if(!correctedData.isEmpty())
                    {
                        processUpdateForCorrectedData(table, listenersList, correctedData, false);
                    }
                }
            }
        });
        LOGGER.info("Time taken to process update action for tables {} {}", dataMap.keySet(), System.currentTimeMillis() - time);
    }

    public Map<Long, Row> processPreUpdateSnapshot(Table table, WhereClause whereClause, Map<Column, Object> modifiedColumns)
    {
        final long time = System.currentTimeMillis();
        final Collection<List<RowUpdateListener>> listenersList = rowListenerContainer.getUpdateListeners(table);
        final Set<String> columns = modifiedColumns.keySet().stream().map(Column::getColumnName).collect(Collectors.toSet());
        if (listenersList != null && !columns.isEmpty() && listenersList.stream().anyMatch( list -> list.stream().anyMatch( listener -> !listener.ignoreUpdate(table, columns))))
        {
            LOGGER.error("RowListener found for table {} to process update action", table);
            LOGGER.info("Fetching complete data from table {} to process update action", table);
            final Map<Long, Row> correctedData = new HashMap<>();
            final SelectQuery query = SelectQuery.get(table);
            query.setLimitClause(new LimitClause(1, UPDATE_FETCH_LIMIT));
            query.setWhereClause(whereClause);
            final DataContainer dataContainer = readableDataStore.get(query);
            LOGGER.info("Total rows retrieved from table {}, to process update action", table);

            dataContainer.getRows(table).forEach( row ->
            {
                modifiedColumns.forEach(row::set);

                correctedData.put(row.getPKValue(), row);
            });
            if(dataContainer.size(table) == UPDATE_FETCH_LIMIT)
            {
                LOGGER.error("Update action watermark reached, cannot process listeners with more than 1000 rows");
            }
            LOGGER.info("Time taken to process pre update snapshot for table {} {}", table, System.currentTimeMillis() - time);
            return correctedData;
        }
        else
        {
            return Collections.emptyMap();
        }
    }

    public void processPostUpdateAction(LinkedHashMap<Table, Map<Long, Row>> snapshot)
    {
        snapshot.forEach((table, data) ->
        {
            final Collection<List<RowUpdateListener>> listenersList = rowListenerContainer.getUpdateListeners(table);
            if(listenersList != null)
            {
                processUpdateForCorrectedData(table, listenersList, data, data.size() == UPDATE_FETCH_LIMIT);
            }
        });
    }

    private void processUpdateForCorrectedData(Table table, Collection<List<RowUpdateListener>> listenersList, Map<Long, Row> correctedData, boolean notifyPossibleOutOfRangeLimit)
    {
        if(correctedData.size() > 0)
        {
            final long time = System.currentTimeMillis();
            final Collection<Row> data = Collections.unmodifiableCollection(correctedData.values());
            for (List<RowUpdateListener> listeners : listenersList)
            {
                for (RowUpdateListener listener : listeners)
                {
                    try
                    {
                        if(notifyPossibleOutOfRangeLimit)
                        {
                            LOGGER.info("Update fetch limit reached for table {}, trying to send notification to listener", table);
                            if(listener.processOutOfRangeNotification())
                            {
                                LOGGER.info("Listener ready to process the request after limit handling, Table : {}", table);
                                listener.updateRows(data);
                            }
                            else
                            {
                                LOGGER.warn("Listener terminated the process after limit notification, Table : {}", table);
                            }
                        }
                        else
                        {
                            listener.updateRows(data);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        throw ErrorCode.getLite(SQLErrorCodes.DB_LISTENER_UPDATE_ACTION_FAILED, throwable);
                    }
                }
            }
            LOGGER.info("Time taken to complete listener update call for table {} {}", table, System.currentTimeMillis() - time);
        }
    }
}
