package com.tlc.sql.api.meta;

import java.util.List;
import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class IndexDefinition
{
    private final String name;
    private final List<String> columns;
    public IndexDefinition(String name, List<String> columns)
    {
        this.name = Objects.requireNonNull(name);
        this.columns = List.copyOf(columns);
    }

    public String getName()
    {
        return name;
    }

    public List<String> getColumns()
    {
        return columns;
    }
}
