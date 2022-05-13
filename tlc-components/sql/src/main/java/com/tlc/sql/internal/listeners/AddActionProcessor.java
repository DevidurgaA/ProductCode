package com.tlc.sql.internal.listeners;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.listeners.RowAddListener;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @author Abishek
 * @version 1.0
 */
public class AddActionProcessor
{
    private final RowListenerContainer rowListenerContainer;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddActionProcessor.class);
    public AddActionProcessor()
    {
        this.rowListenerContainer = RowListenerContainer.getInstance();
    }

    public void processAddAction(SortedMap<Table, Map<Long, Row>> dataMap)
    {
        final long time = System.currentTimeMillis();
        final Set<Table> tables = Collections.unmodifiableSet(dataMap.keySet());
        dataMap.forEach( (table, rowInfo) -> processAddAction(table, rowInfo.values(), tables));
        LOGGER.debug("Time taken to process update action for tables {} {}", dataMap.keySet(), System.currentTimeMillis() - time);
    }

    private void processAddAction(Table table, Collection<Row> rows, Set<Table> tables)
    {
        final Collection<List<RowAddListener>> listenersList = rowListenerContainer.getAddListeners(table);
        if(listenersList != null && rows.size() > 0)
        {
            final long time = System.currentTimeMillis();
            LOGGER.debug("RowListener found for table {} to process add action", table);
            final Collection<Row> unmodifiableRow = Collections.unmodifiableCollection(rows);
            for (List<RowAddListener> listeners : listenersList)
            {
                for (RowAddListener listener : listeners)
                {
                    try
                    {
                        if(!listener.ignore(tables))
                        {
                            listener.addRows(unmodifiableRow);
                        }
                    }
                    catch (Throwable throwable)
                    {
                        throw ErrorCode.getLite(SQLErrorCodes.DB_LISTENER_ADD_ACTION_FAILED, throwable);
                    }
                }
            }
            LOGGER.info("Time taken to complete listener add call for table {} {}", table, System.currentTimeMillis() - time);
        }
    }

}
