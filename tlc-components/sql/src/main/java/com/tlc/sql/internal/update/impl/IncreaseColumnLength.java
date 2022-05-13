package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class IncreaseColumnLength implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final ColumnDefinition columnDefinition;
    private final ColumnDefinition oldColumnDef;

    public IncreaseColumnLength(TableDefinition tableDefinition, ColumnDefinition oldColumnDef, ColumnDefinition columnDefinition)
    {
        this.columnDefinition = Objects.requireNonNull(columnDefinition);
        this.oldColumnDef = Objects.requireNonNull(oldColumnDef);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateColumnLengthSQL(tableDefinition, columnDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateColumnLengthSQL(tableDefinition, oldColumnDef);
    }
}
