package com.tlc.sql.api.listeners;

import com.tlc.sql.api.dml.Table;

import java.util.Set;


/**
 * @author Abishek
 * @version 1.0
 */
public interface IgnoreIfExists
{
    default boolean ignore(Set<Table> affectedTables)
    {
        return false;
    }

}
