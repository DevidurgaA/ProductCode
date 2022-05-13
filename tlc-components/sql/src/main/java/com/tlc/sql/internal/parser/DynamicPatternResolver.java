package com.tlc.sql.internal.parser;

import com.tlc.sql.api.dml.Criteria;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.api.ds.ReadableDataStore;
import com.tlc.sql.resource.FGSEQUENCEPATTERN;

import java.util.*;


/**
 * @author Abishek
 * @version 1.0
 */
public class DynamicPatternResolver extends StaticPatternResolver
{
    private final Set<String> loadedTables;
    private final ReadableDataStore readableDataStore;
    private final Table sequenceTable;
    public DynamicPatternResolver(ReadableDataStore readableDataStore)
    {
        this.sequenceTable = Table.get(FGSEQUENCEPATTERN.TABLE);
        this.loadedTables = new HashSet<>();
        this.readableDataStore = Objects.requireNonNull(readableDataStore);
    }

    @Override
    public Long getValue(String tableName, String pattern)
    {
        if(loadedTables.contains(tableName))
        {
            return super.getValue(tableName, pattern);
        }
        else
        {
            final Map<String, Long> map = patternMap.computeIfAbsent(tableName, k -> new HashMap<>());
            final WhereClause whereClause = new WhereClause(Criteria.eq(sequenceTable.getColumn(FGSEQUENCEPATTERN.TABLE_NAME), tableName));
            readableDataStore.fetchData(sequenceTable, whereClause, row ->
            {
                final String patternL = (String) row.get(FGSEQUENCEPATTERN.PATTERN);
                final Long value = (Long) row.get(FGSEQUENCEPATTERN.COMMIT_VALUE);
                map.put(patternL, value);
            });
            loadedTables.add(tableName);
            return map.get(pattern);
        }
    }
}
