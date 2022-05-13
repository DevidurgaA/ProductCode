package com.tlc.sql.api.listeners;

import com.tlc.sql.api.Row;

import java.util.Collection;


/**
 * @author Abishek
 * @version 1.0
 */
public interface RowAddListener extends IgnoreIfExists, ListenerOrder
{
    void addRows(Collection<Row> rows);
}
