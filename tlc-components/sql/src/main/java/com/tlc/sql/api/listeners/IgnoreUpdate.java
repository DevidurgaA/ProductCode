package com.tlc.sql.api.listeners;

import com.tlc.sql.api.dml.Table;

import java.util.Set;


/**
 * @author Abishek
 * @version 1.0
 */
public interface IgnoreUpdate extends IgnoreIfExists
{
    default boolean ignoreUpdate(Table table, Set<String> columns)
    {
        return false;
    }
}
