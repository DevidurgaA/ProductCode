package com.tlc.sql.update.ddl;


import com.tlc.sql.internal.handler.DDLHandler;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DDLAction
{
    String[] getSQL(DDLHandler ddlHandler);
}
