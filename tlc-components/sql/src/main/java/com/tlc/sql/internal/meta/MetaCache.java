package com.tlc.sql.internal.meta;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheManager;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Map;
import java.util.Set;

/**
 * @author Abishek
 * @version 1.0
 */
public class MetaCache
{
    private static final MetaCache INSTANCE;
    static
    {
        INSTANCE = new MetaCache();
    }

    private final Cache<String, TableDefinition> table_definitions;

    private MetaCache()
    {
        this.table_definitions = CacheManager.getInstance().createCache();
    }

    public static MetaCache get()
    {
        return INSTANCE;
    }

    void put(Map<String, TableDefinition> tableDefinitions)
    {
        table_definitions.putAll(tableDefinitions);
    }

    void put(TableDefinition tableDefinition)
    {
        table_definitions.put(tableDefinition.getName(), tableDefinition);
    }

    void remove(String tableName)
    {
        table_definitions.remove(tableName);
    }

    void remove(Set<String> tableNames)
    {
        tableNames.forEach(table_definitions::remove);
    }

    public TableDefinition get(String tableName)
    {
        return table_definitions.get(tableName);
    }
}
