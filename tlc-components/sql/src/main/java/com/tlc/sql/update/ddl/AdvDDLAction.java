package com.tlc.sql.update.ddl;

import com.tlc.sql.internal.handler.DDLHandler;

/**
 * @author Abishek
 * @version 1.0
 */
public interface AdvDDLAction extends DDLAction
{
    String[] getRevertSQL(DDLHandler ddlHandler);
}
