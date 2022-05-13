package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class CreateTable implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    public CreateTable(TableDefinition tableDefinition)
    {
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getCreateTableSQL(tableDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getDropTableSQL(tableDefinition);
        return new String[]{sql};
    }

}
