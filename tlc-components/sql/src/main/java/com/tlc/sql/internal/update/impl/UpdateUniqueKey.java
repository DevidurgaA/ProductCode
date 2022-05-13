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
public class UpdateUniqueKey implements AdvDDLAction
{

    private final UKDefinition oldUkDefinition;
    private final UKDefinition ukDefinition;
    private final TableDefinition tableDefinition;
    public UpdateUniqueKey(TableDefinition tableDefinition, UKDefinition oldUkDefinition, UKDefinition ukDefinition)
    {
        this.ukDefinition = Objects.requireNonNull(ukDefinition);
        this.oldUkDefinition = Objects.requireNonNull(oldUkDefinition);
        this.tableDefinition = Objects.requireNonNull(tableDefinition);
    }

    @Override
    public String[] getSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateUniqueKeySQL(tableDefinition, ukDefinition);
    }

    @Override
    public String[] getRevertSQL(DDLHandler ddlHandler)
    {
        return ddlHandler.getUpdateUniqueKeySQL(tableDefinition, oldUkDefinition);
    }
}
