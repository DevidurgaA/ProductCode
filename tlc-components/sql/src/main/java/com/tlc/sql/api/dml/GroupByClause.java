package com.tlc.sql.api.dml;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class GroupByClause
{
    private final Column column;

    public GroupByClause(Column column)
    {
        this.column = Objects.requireNonNull(column);
    }

    public Column getColumn()
    {
        return column;
    }
}
