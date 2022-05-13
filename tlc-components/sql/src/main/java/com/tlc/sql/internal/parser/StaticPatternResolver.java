package com.tlc.sql.internal.parser;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public class StaticPatternResolver implements PatternResolver
{
    protected final Map<String, Map<String, Long>> patternMap;
    public StaticPatternResolver()
    {
        patternMap = new HashMap<>();
    }

    @Override
    public Long getValue(String tableName, String pattern)
    {
        final Map<String, Long> seqIdMap = patternMap.get(tableName);
        if(seqIdMap == null)
        {
            return null;
        }
        else
        {
            return seqIdMap.get(pattern);
        }
    }

    @Override
    public boolean patternExists(String tableName, String pattern)
    {
        return getValue(tableName, pattern) != null;
    }

    @Override
    public void addValue(String tableName, String pattern, Long value)
    {
        patternMap.computeIfAbsent(tableName, k -> new HashMap<>()).put(pattern, value);
    }
}
