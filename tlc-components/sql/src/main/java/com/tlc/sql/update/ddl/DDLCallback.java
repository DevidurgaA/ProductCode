package com.tlc.sql.update.ddl;


/**
 * @author Abishek
 * @version 1.0
 */
public interface DDLCallback
{
    void preDDLAction();

    void postDDLAction();
}
