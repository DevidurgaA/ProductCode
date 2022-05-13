package com.tlc.sql.api.listeners;

import com.tlc.sql.api.dml.Table;

import java.util.Collection;


/**
 * @author Abishek
 * @version 1.0
 */
public interface IgnoreDelete extends IgnoreIfExists
{
    default boolean ignoreParentDeleteAction(Table table, Collection<Long> parentPk)
    {
        return false;
    }
}
