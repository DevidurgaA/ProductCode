package com.tlc.sql.internal.update.impl;

import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.api.meta.UKDefinition;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class DropUniqueKey implements AdvDDLAction
{
    private final TableDefinition tableDefinition;
    private final UKDefinition ukDefinition;

    public DropUniqueKey(TableDefinition tableDefinition, UKDefinition ukDefinition)
    {
        this.ukDefinition = Objects.requireNonNull(ukDefinition);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getDropUniqueKeySQL(tableDefinition, ukDefinition);
        return new String[] {sql};
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        final String sql = ddlHandler.getAddUniqueKeySQL(tableDefinition, ukDefinition);
        return new String[] {sql};
    }
}
