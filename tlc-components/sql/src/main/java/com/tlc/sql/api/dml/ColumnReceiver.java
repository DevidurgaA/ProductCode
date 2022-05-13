package com.tlc.sql.api.dml;


/**
 * @author Abishek
 * @version 1.0
 */
public interface ColumnReceiver
{
    void process(Object value);
}