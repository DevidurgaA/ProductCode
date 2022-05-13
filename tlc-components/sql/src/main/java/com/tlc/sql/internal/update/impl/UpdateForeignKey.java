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
public class UpdateForeignKey implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final FKDefinition oldFkDefinition;
    private final FKDefinition fkDefinition;

    public UpdateForeignKey(TableDefinition tableDefinition, FKDefinition oldFkDefinition, FKDefinition fkDefinition)
    {
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
        this.oldFkDefinition = Objects.requireNonNull(oldFkDefinition);
        this.fkDefinition = Objects.requireNonNull(fkDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateForeignKeySQL(tableDefinition, fkDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateForeignKeySQL(tableDefinition, oldFkDefinition);
    }
}
