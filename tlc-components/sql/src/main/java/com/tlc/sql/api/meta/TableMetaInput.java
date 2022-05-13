package com.tlc.sql.api.meta;

import java.util.*;


/**
 * @author Abishek
 * @version 1.0
 */
public class TableMetaInput
{
    private final Map<String, ColumnDefinition> columnDefinitions;
    private final Map<String, FKDefinition> fkDefinitions;
    private final Map<String, UKDefinition> ukDefinitions;
    private final Map<String, IndexDefinition> indexDefinitions;
    private final Set<String> childTables;

    private final String tableName;
    private PKDefinition pkDefinition;
    public TableMetaInput(String tableName)
    {
        this.tableName = Objects.requireNonNull(tableName);
        this.fkDefinitions = new HashMap<>();
        this.ukDefinitions = new HashMap<>();
        this.columnDefinitions = new HashMap<>();
        this.indexDefinitions = new HashMap<>();
        this.childTables = new HashSet<>();
    }

    public void setPkDefinition(PKDefinition pkDefinition)
    {
        this.pkDefinition = pkDefinition;
    }

    public String getTableName()
    {
        return tableName;
    }

    private void addChildTable(String tableName)
    {
        childTables.add(tableName);
    }

    public void addFKDefinition(FKDefinition fkdefinition)
    {
        fkDefinitions.put(fkdefinition.getName(), fkdefinition);
    }

    public void addUKDefinition(UKDefinition ukdefinition)
    {
        ukDefinitions.put(ukdefinition.getName(), ukdefinition);
    }

    public void addColumnDefinition(ColumnDefinition columnDefinition)
    {
        columnDefinitions.put(columnDefinition.getColumnName(), columnDefinition);
    }

    public void addIndexDefinition(IndexDefinition indexDefinition)
    {
        indexDefinitions.put(indexDefinition.getName(), indexDefinition);
    }

    public PKDefinition getPkDefinition()
    {
        return pkDefinition;
    }

    public Map<String, ColumnDefinition> getColumnDefinitions()
    {
        return columnDefinitions;
    }

    public Map<String, FKDefinition> getFkDefinitions()
    {
        return fkDefinitions;
    }

    public Map<String, UKDefinition> getUkDefinitions()
    {
        return ukDefinitions;
    }

    public Map<String, IndexDefinition> getIndexDefinitions()
    {
        return indexDefinitions;
    }

    public Set<String> getChildTables()
    {
        return childTables;
    }

    public ColumnDefinition getColumnDefinition(String localColumn)
    {
        return columnDefinitions.get(localColumn);
    }

    public Set<String> getColumns()
    {
        return columnDefinitions.keySet();
    }
}
