package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.IndexDefinition;
import com.tlc.sql.api.meta.TableDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class CreateIndex implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final IndexDefinition indexDefinition;
    public CreateIndex(TableDefinition tableDefinition, IndexDefinition indexDefinition)
    {
        this.indexDefinition = Objects.requireNonNull(indexDefinition);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getAddIndexSQL(tableDefinition, indexDefinition);
        return new String[]{sql};
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getDropIndexSQL(tableDefinition, indexDefinition);
        return new String[]{sql};
    }
}
