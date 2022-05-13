package com.tlc.sql.internal.parser;

import com.tlc.sql.api.meta.TableDefinition;


/**
 * @author Abishek
 * @version 1.0
 */
public interface TableDefinitionLoader
{
    boolean isDuplicate(String tableName);

    void add(TableDefinition tableDefinition);

    TableDefinition resolve(String tableName);

    default boolean loadKeys() { return true; }
}
