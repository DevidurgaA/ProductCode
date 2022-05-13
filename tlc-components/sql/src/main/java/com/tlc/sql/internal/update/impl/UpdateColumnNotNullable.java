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
public class UpdateColumnNotNullable implements AdvDDLAction
{
    private final ColumnDefinition columnDefinition;
    private final TableDefinition tableDefinition;

    public UpdateColumnNotNullable(TableDefinition tableDefinition, ColumnDefinition columnDefinition)
    {
        this.columnDefinition = Objects.requireNonNull(columnDefinition);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getChangeColumnNotNullableSQL(tableDefinition, columnDefinition);
        return new String[] {sql};
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getChangeColumnNullableSQL(tableDefinition, columnDefinition);
        return new String[] {sql};
    }
}
