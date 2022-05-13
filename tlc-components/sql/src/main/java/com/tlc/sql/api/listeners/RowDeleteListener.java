package com.tlc.sql.api.listeners;

import com.tlc.sql.api.Row;

import java.util.Collection;


/**
 * @author Abishek
 * @version 1.0
 */
public interface RowDeleteListener extends IgnoreDelete, OutOfRangeNotification, ListenerOrder
{
    void deleteRows(Collection<Row> rows);
}
