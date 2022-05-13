package com.tlc.sql.api.dml;

import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public interface RowReceiver
{
    void process(Map<String, Object> row);
}