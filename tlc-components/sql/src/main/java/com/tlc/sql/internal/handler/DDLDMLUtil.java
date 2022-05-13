package com.tlc.sql.internal.handler;

import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.TableDefinition;


/**
 * @author Abishek
 * @version 1.0
 */
class DDLDMLUtil
{
    static String getTableName(String tableName)
    {
        return "\""+ tableName +"\"";
    }

    static String getTableName(TableDefinition tableDefinition)
    {
        return "\""+ tableDefinition.getLowercaseName() +"\"";
    }

    static String getTableName(Table table)
    {
        return "\""+ table.getAlias() +"\"";
    }

    static String getColumnName(ColumnDefinition columnDefinition)
    {
        return getColumnName(columnDefinition.getColumnName());
    }

    static String getColumnName(String column)
    {
        return "\"" + column + "\"";
    }

}
