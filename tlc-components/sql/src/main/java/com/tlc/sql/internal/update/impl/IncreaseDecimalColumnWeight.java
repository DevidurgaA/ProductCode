package com.tlc.sql.internal.update.impl;

import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.handler.DDLHandler;
import com.tlc.sql.update.ddl.AdvDDLAction;

/**
 * {@inheritDoc}
 *
 * @author Sundar
 * @version 1.0
 */
public class IncreaseDecimalColumnWeight implements AdvDDLAction {

    private final TableDefinition tableDefinition;
    private final ColumnDefinition columnDefinition;
    private final ColumnDefinition oldColumnDefinition;

    public IncreaseDecimalColumnWeight(TableDefinition tableDefinition, ColumnDefinition columnDefinition,
                                          ColumnDefinition oldColumnDefinition) {
        this.tableDefinition = tableDefinition;
        this.columnDefinition = columnDefinition;
        this.oldColumnDefinition = oldColumnDefinition;
    }

    /**
     * {@inheritDoc}
     *
     * @param ddlHandler Data definition handler
     * @return The SQL queries to increase decimal weight
     */
    @Override
    public String[] getSQL(final DDLHandler ddlHandler) {
        return ddlHandler.getIncreaseDecimalWeightSQL(tableDefinition, columnDefinition);
    }

    /**
     * {@inheritDoc}
     *
     * @param ddlHandler Data definition handler
     * @return The SQL queries to revert action
     */
    @Override
    public String[] getRevertSQL(final DDLHandler ddlHandler) {
        return ddlHandler.getIncreaseDecimalWeightSQL(tableDefinition, oldColumnDefinition);
    }
}
