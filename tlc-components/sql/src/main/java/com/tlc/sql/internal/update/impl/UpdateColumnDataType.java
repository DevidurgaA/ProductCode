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
public class UpdateColumnDataType implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final ColumnDefinition oldColumnDefinition;
    private final ColumnDefinition columnDefinition;
    public UpdateColumnDataType(TableDefinition tableDefinition, ColumnDefinition oldColumnDefinition, ColumnDefinition columnDefinition)
    {
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
        this.oldColumnDefinition = Objects.requireNonNull(oldColumnDefinition);
        this.columnDefinition = Objects.requireNonNull(columnDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getChangeColumnDataTypeSQL(tableDefinition, columnDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getChangeColumnDataTypeSQL(tableDefinition, oldColumnDefinition);
    }
}
