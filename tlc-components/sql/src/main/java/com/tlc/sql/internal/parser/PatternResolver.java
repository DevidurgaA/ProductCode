package com.tlc.sql.internal.parser;


/**
 * @author Abishek
 * @version 1.0
 */
public interface PatternResolver
{
    boolean patternExists(String tableName, String pattern);

    Long getValue(String tableName, String pattern);

    void addValue(String tableName, String pattern, Long value);
}
