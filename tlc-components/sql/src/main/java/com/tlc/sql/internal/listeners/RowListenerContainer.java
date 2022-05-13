package com.tlc.sql.internal.listeners;

import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.listeners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Abishek
 * @version 1.0
 */
class RowListenerContainer
{
    private final Map<Table, SortedMap<ListenerOrder.Priority, List<RowAddListener>>> addListenerCache;
    private final Map<Table, SortedMap<ListenerOrder.Priority, List<RowUpdateListener>>> updateListenerCache;
    private final Map<Table, SortedMap<ListenerOrder.Priority, List<RowDeleteListener>>> deleteListenerCache;
    private final Map<Table, SortedMap<ListenerOrder.Priority, List<RowIdDeleteListener>>> idDeleteListenerCache;

    private static final Logger LOGGER = LoggerFactory.getLogger(RowListenerContainer.class);
    private final static class InstanceHolder
    {
        private static final RowListenerContainer INSTANCE = new RowListenerContainer();
    }

    static RowListenerContainer getInstance()
    {
        return InstanceHolder.INSTANCE;
    }

    private RowListenerContainer()
    {
        this.updateListenerCache = new HashMap<>();
        this.addListenerCache = new HashMap<>();
        this.deleteListenerCache = new HashMap<>();
        this.idDeleteListenerCache = new HashMap<>();
    }

    void addAddListener(Table table, RowAddListener listener)
    {
        LOGGER.info("RowAddListener received from {}", listener.getClass().getName());
        addListenerCache.computeIfAbsent(table, k -> new TreeMap<>()).computeIfAbsent(listener.getPriority(), k->
                new CopyOnWriteArrayList<>()).add(listener);
    }

    void addUpdateListener(Table table, RowUpdateListener listener)
    {
        LOGGER.info("RowUpdateListener received from {}", listener.getClass().getName());
        updateListenerCache.computeIfAbsent(table, k -> new TreeMap<>()).computeIfAbsent(listener.getPriority(), k->
                new CopyOnWriteArrayList<>()).add(listener);
    }

    void addDeleteListener(Table table, RowIdDeleteListener listener)
    {
        LOGGER.info("RowIDDeleteListener received from {}", listener.getClass().getName());
        idDeleteListenerCache.computeIfAbsent(table, k -> new TreeMap<>()).computeIfAbsent(listener.getPriority(), k->
                new CopyOnWriteArrayList<>()).add(listener);
    }

    void addDeleteListener(Table table, RowDeleteListener listener)
    {
        LOGGER.info("RowDeleteListener received from {}", listener.getClass().getName());
        deleteListenerCache.computeIfAbsent(table, k -> new TreeMap<>()).computeIfAbsent(listener.getPriority(), k->
                new CopyOnWriteArrayList<>()).add(listener);
    }

    Collection<List<RowAddListener>> getAddListeners(Table table)
    {
        final Map<ListenerOrder.Priority, List<RowAddListener>> listenersMap = addListenerCache.get(table);
        if(listenersMap != null)
        {
            return listenersMap.values();
        }
        else
        {
            return null;
        }

    }

    Collection<List<RowUpdateListener>> getUpdateListeners(Table table)
    {
        final Map<ListenerOrder.Priority, List<RowUpdateListener>> listenersMap = updateListenerCache.get(table);
        if(listenersMap != null)
        {
            return listenersMap.values();
        }
        else
        {
            return null;
        }

    }

    Collection<List<RowDeleteListener>> getDeleteListeners(Table table)
    {
        final Map<ListenerOrder.Priority, List<RowDeleteListener>> listenersMap = deleteListenerCache.get(table);
        if(listenersMap != null)
        {
            return listenersMap.values();
        }
        else
        {
            return null;
        }
    }

    Collection<List<RowIdDeleteListener>> getDeleteListeners_pk(Table table)
    {
        final Map<ListenerOrder.Priority, List<RowIdDeleteListener>> listenersMap = idDeleteListenerCache.get(table);
        if(listenersMap != null)
        {
            return listenersMap.values();
        }
        else
        {
            return null;
        }
    }

    boolean hasDeleteListeners(Table table)
    {
        return deleteListenerCache.containsKey(table);
    }

    boolean hasDeleteListeners_pk(Table table)
    {
        return idDeleteListenerCache.containsKey(table);
    }

}
