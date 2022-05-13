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
public class UpdateIndex implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final IndexDefinition oldIndexDefinition;
    private final IndexDefinition indexDefinition;
    public UpdateIndex(TableDefinition tableDefinition, IndexDefinition oldIndexDefinition, IndexDefinition indexDefinition)
    {
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
        this.oldIndexDefinition = Objects.requireNonNull(oldIndexDefinition);
        this.indexDefinition = Objects.requireNonNull(indexDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateIndexSQL(tableDefinition, indexDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateIndexSQL(tableDefinition, oldIndexDefinition);
    }
}
