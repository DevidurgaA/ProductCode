package com.tlc.sql.internal.handler;

import com.tlc.sql.api.meta.*;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DDLHandler
{
    String getInitializeSequenceSQL(String sequenceName, int start, int incrementBy);

    String getCreateSequenceSQL(String sequenceName, int start, int incrementBy);

    String getUpdateSequenceSQL(String sequenceName, int incrementBy);

    String getDropSequenceSQL(String sequenceName);

    String[] getCreateTableSQL(TableDefinition tableDefinition);

    String getAddColumnSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String getAddForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition);

    String getAddIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition);

    String getAddUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition);

    String getDropColumnSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String getDropForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition);

    String getDropIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition);

    String getDropUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition);

    String getDropTableSQL(TableDefinition tableDefinition);

    String[] getUpdateColumnLengthSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String[] getChangeColumnDataTypeSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String getChangeColumnNotNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String getChangeColumnNullableSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);

    String[] getUpdateForeignKeySQL(TableDefinition tableDefinition, FKDefinition fkDefinition);

    String[] getUpdateIndexSQL(TableDefinition tableDefinition, IndexDefinition indexDefinition);

    String[] getUpdateUniqueKeySQL(TableDefinition tableDefinition, UKDefinition ukDefinition);

    /**
     * Gets the query to alter the decimal data type weight
     *
     * @param tableDefinition Table definition
     * @param columnDefinition Column definition
     * @return The query to alter the decimal data type weight
     */
    String[] getIncreaseDecimalWeightSQL(TableDefinition tableDefinition, ColumnDefinition columnDefinition);
}
