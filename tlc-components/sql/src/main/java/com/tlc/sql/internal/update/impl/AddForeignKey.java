package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.FKDefinition;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class AddForeignKey implements AdvDDLAction
{
    private final FKDefinition fkDefinition;
    private final TableDefinition tableDefinition;
    public AddForeignKey(TableDefinition tableDefinition, FKDefinition fkDefinition)
    {
        this.fkDefinition = Objects.requireNonNull(fkDefinition);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getAddForeignKeySQL(tableDefinition, fkDefinition);
        return new String[]{sql};
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getDropForeignKeySQL(tableDefinition, fkDefinition);
        return new String[]{sql};
    }
}
