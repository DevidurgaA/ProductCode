//package com.tlc.sql.upgrade;
//
//import com.tlc.commons.code.ErrorCode;
//import com.tlc.sql.api.DataContainer;
//import com.tlc.sql.api.Row;
//import com.tlc.sql.api.dml.DeleteQuery;
//import com.tlc.sql.api.dml.Table;
//import com.tlc.sql.internal.meta.ColumnDefinition;
//import com.tlc.sql.internal.meta.FKDefinition;
//import com.tlc.sql.internal.meta.TableDefinition;
//import com.tlc.sql.internal.parser.DynamicDataProvider;
//import com.tlc.sql.internal.parser.XmlToDCConverter;
//
//import java.io.File;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class UpdateManager
//{
//    private final DistributedLock lock;
//    private final Cache<Table, Collection<TableChangeListener>> listenerCache;
//    private final com.alecmo.updatemgr.sql.DMLUpdateManager dmlUpdateManager;
//    public UpdateManager(DistributedLock distributedLock, DMLUpdateManager dmlUpdateManager, Cache<Table, Collection<TableChangeListener>> listenerCache)
//    {
//        this.lock = Objects.requireNonNull(distributedLock);
//        this.dmlUpdateManager = Objects.requireNonNull(dmlUpdateManager);
//        this.listenerCache = Objects.requireNonNull(listenerCache);
//    }
//
//    public void safeInstall(List<File> files, DynamicDataProvider dyProvider, PKValidator pkValidator, Set<Long> allowedTables, PreCommitCallback callback) throws Exception
//    {
//        lock.acquire();
//        try
//        {
//            final DataContainer dataContainer = install(files, pkValidator, allowedTables, dyProvider, callback);
//            final Set<TableChangeListener> listeners = new HashSet<>();
//            dataContainer.getTables().forEach( table ->
//            {
//                final Table tableObj = Table.get(table);
//                final Collection<TableChangeListener> tableListeners = listenerCache.get(tableObj);
//                if(tableListeners != null)
//                {
//                    listeners.addAll(tableListeners);
//                }
//            });
//            listeners.forEach(TableChangeListener::changeNotification);
//        }
//        finally
//        {
//            lock.release();
//        }
//    }
//
//    public void safeUpgrade(List<File> newFiles, DynamicDataProvider newDy, List<File> oldFiles, DynamicDataProvider oldDy,
//                            PKValidator pkValidator, Set<Long> allowedTables, PreCommitCallback callback) throws Exception
//    {
//        lock.acquire();
//        try
//        {
//            final DataContainer dataContainer = upgrade(newFiles, newDy, oldFiles, oldDy, pkValidator, allowedTables, callback);
//            final Set<TableChangeListener> listeners = new HashSet<>();
//            dataContainer.getTables().forEach( table ->
//            {
//                final Table tableObj = Table.get(table);
//                final Collection<TableChangeListener> tableListeners = listenerCache.get(tableObj);
//                if(tableListeners != null)
//                {
//                    listeners.addAll(tableListeners);
//                }
//            });
//            listeners.forEach(TableChangeListener::changeNotification);
//        }
//        finally
//        {
//            lock.release();
//        }
//    }
//
//    public void safeUninstall(List<File> files, Set<Long> allowedTables) throws Exception
//    {
//        lock.acquire();
//        try
//        {
//            final Collection<Table> tables = uninstall(files, allowedTables);
//            final Set<TableChangeListener> listeners = new HashSet<>();
//            tables.forEach( table ->
//            {
//                final Collection<TableChangeListener> tableListeners = listenerCache.get(table);
//                if(tableListeners != null)
//                {
//                    listeners.addAll(tableListeners);
//                }
//            });
//            listeners.forEach(TableChangeListener::changeNotification);
//        }
//        finally
//        {
//            lock.release();
//        }
//    }
//
//    private Collection<Table> uninstall(List<File> files, Set<Long> allowedTables) throws Exception
//    {
//        final Map<String, Long> tableNames = getTableNames(allowedTables);
//        final Map<String, Row> patternMap = getRelatedTablePatternRow(tableNames.keySet());
//
//        final XmlToDCConverter xmlToDCConverter = XmlToDCConverter.getInstance();
//
//        final NavigableMap<Table, NavigableSet<Long>> pkValuesMap = new TreeMap<>();
//        for (File file : files)
//        {
//            final Map<Table, Set<Long>> pkMap = xmlToDCConverter.getRowPrimaryKeys(file, patternMap);
//            pkMap.forEach( (table, pkIds) ->
//                    pkValuesMap.computeIfAbsent(table, k -> new TreeSet<>()).addAll(pkIds));
//        }
//        final Collection<String> tables =  pkValuesMap.keySet().stream().map(Table::getTableName).collect(Collectors.toList());
//        if(tables.stream().anyMatch( x -> !tableNames.containsKey(x)))
//        {
//            throw ErrorCode.get(PredefinedErrorCodes.ACCESS_DENIED);
//        }
//
//        final Map<Long, com.alecmo.updatemgr.sql.DataModifier> formatterMap = getFormatterMap(allowedTables);
//        tables.forEach( table ->
//        {
//            final Long tableId = tableNames.get(table);
//            final com.alecmo.updatemgr.sql.DataModifier dataModifier = formatterMap.get(tableId);
//            if(dataModifier != null)
//            {
//                dataModifier.modifyForUnInstall(pkValuesMap);
//            }
//        });
//
//        final List<Table> modifiedTable = new LinkedList<>(pkValuesMap.keySet());
//
//        final Set<Table> duplicateTables = getDuplicateTables(pkValuesMap);
//        duplicateTables.forEach(pkValuesMap::remove);
//        DBAccessProvider.getDataStore().delete(pkValuesMap);
//        return modifiedTable;
//    }
//
//    private DataContainer install(List<File> files, PKValidator pkValidator, Set<Long> allowedTables, DynamicDataProvider dyProvider, PreCommitCallback callback) throws Exception
//    {
//        final Map<String, Long> tableNames = getTableNames(allowedTables);
//        final Map<String, Long> patternMap = getRelatedTablePatternValue(tableNames.keySet());
//
//        final XmlToDCConverter xmlToDCConverter = XmlToDCConverter.getInstance();
//        final DataContainer dataContainer = DataContainer.create();
//        for (File file : files)
//        {
//            dataContainer.append(xmlToDCConverter.convert(file, patternMap, pkValidator, dyProvider, XmlToDCConverter.Type.INITIALIZE));
//        }
//        final Set<String> tables =  dataContainer.getTables();
//        if(tables.stream().anyMatch( x -> !tableNames.containsKey(x)))
//        {
//            throw ErrorCode.get(PredefinedErrorCodes.ACCESS_DENIED);
//        }
//        final Map<Long, com.alecmo.updatemgr.sql.DataModifier> formatterMap = getFormatterMap(allowedTables);
//        tables.forEach( table ->
//        {
//            final Long tableId = tableNames.get(table);
//            final com.alecmo.updatemgr.sql.DataModifier dataModifier = formatterMap.get(tableId);
//            if(dataModifier != null)
//            {
//                dataModifier.modifyForInstall(dataContainer);
//            }
//        });
//        callback.call(dataContainer);
//        DBAccessProvider.getDataStore().commitChanges(dataContainer);
//        return dataContainer;
//    }
//
//    private DataContainer upgrade(List<File> newFiles, DynamicDataProvider newDy, List<File> oldFiles, DynamicDataProvider oldDy,
//                                  PKValidator pkValidator, Set<Long> allowedTables, PreCommitCallback callback) throws Exception
//    {
//
//        final Map<String, Long> tableNames = getTableNames(allowedTables);
//        final Map<String, Long> patternMap = getRelatedTablePatternValue(tableNames.keySet());
//
//        final XmlToDCConverter xmlToDCConverter = XmlToDCConverter.getInstance();
//        final DataContainer oldDC = DataContainer.create();
//        for (File file : oldFiles)
//        {
//            oldDC.append(xmlToDCConverter.convert(file, patternMap, pkValidator, oldDy, XmlToDCConverter.Type.FETCH));
//        }
//        final DataContainer dDc = dmlUpdateManager.getDataFromDatabase(oldDC, Set.of());
//
//        final DataContainer newDC = DataContainer.create();
//        for (File file : newFiles)
//        {
//            newDC.append(xmlToDCConverter.convert(file, patternMap, pkValidator, newDy, XmlToDCConverter.Type.MIXED));
//        }
//        final Set<String> tables =  newDC.getTables();
//        if(tables.stream().anyMatch( x -> !tableNames.containsKey(x)))
//        {
//            throw ErrorCode.get(PredefinedErrorCodes.ACCESS_DENIED);
//        }
//
//        final Map<Long, com.alecmo.updatemgr.sql.DataModifier> formatterMap = getFormatterMap(allowedTables);
//        tables.forEach( table ->
//        {
//            final Long tableId = tableNames.get(table);
//            final com.alecmo.updatemgr.sql.DataModifier dataModifier = formatterMap.get(tableId);
//            if(dataModifier != null)
//            {
//                dataModifier.modifyForInstall(newDC);
//            }
//        });
//
//        final DataContainer tobeUpdated = DataContainer.create();
//        final List<DeleteQuery> seqQueries = new LinkedList<>();
//        dmlUpdateManager.loadChanges(tobeUpdated, seqQueries, oldDC, dDc, newDC, Set.of());
//
//        final Set<String> tobeModifiedTables =  tobeUpdated.getTables();
//        tobeModifiedTables.forEach( table ->
//        {
//            final Long tableId = tableNames.get(table);
//            final com.alecmo.updatemgr.sql.DataModifier dataModifier = formatterMap.get(tableId);
//            if(dataModifier != null)
//            {
//                dataModifier.modifyForUpgrade(tobeUpdated);
//            }
//        });
//
//
//        callback.call(tobeUpdated);
//        DBAccessProvider.getDataStore().commitChanges(tobeUpdated);
//        return tobeUpdated;
//    }
//
//    private Map<String, Long> getTableNames(Set<Long> allowedTables)
//    {
//        final Table table = Table.get(MCRTABLEINFO.TABLE);
//        final CriteriaBuilder<SQLCriteria> whereClause = new CriteriaBuilder<>(SQLCriteria.in(table.getColumn(MCRTABLEINFO.ID), allowedTables));
//        final DataContainer dataContainer = DBAccessProvider.getDataStore().get(table, whereClause);
//        return dataContainer.getRows(table).collect(Collectors.toMap( row -> row.get(MCRTABLEINFO.NAME), row -> row.get(MCRTABLEINFO.ID)));
//    }
//
//    private Set<Table> getDuplicateTables(NavigableMap<Table, NavigableSet<Long>> tableMap)
//    {
//        final Set<Table> tobeRemoved = new HashSet<>();
//        tableMap.forEach( (table, ids) ->
//        {
//            final TableDefinition tableDefinition = table.getTableDefinition();
//            final Collection<FKDefinition> fkDefinitions = tableDefinition.getFKDefinitions().values();
//            for (FKDefinition fkDefinition : fkDefinitions)
//            {
//                final Table referenceTable = Table.get(fkDefinition.getReferenceTable());
//                final ColumnDefinition columnDefinition = fkDefinition.getLocal();
//                if(fkDefinition.getConstraint() == FKDefinition.FKConstraint.ON_DELETE_CASCADE && !columnDefinition.isNullable() && tableMap.containsKey(referenceTable))
//                {
//                    tobeRemoved.add(table);
//                    break;
//                }
//            }
//        });
//        return tobeRemoved;
//    }
//
//    private Map<Long, com.alecmo.updatemgr.sql.DataModifier> getFormatterMap(Set<Long> allowedTables) throws Exception
//    {
//        final Table table = Table.get(MAPUPDATEDATAMODIFIER.TABLE);
//        final CriteriaBuilder<SQLCriteria> whereClause = new CriteriaBuilder<>(SQLCriteria.in(table.getColumn(MAPUPDATEDATAMODIFIER.TABLE_ID), allowedTables));
//        final DataContainer dataContainer = DBAccessProvider.getDataStore().get(table, whereClause);
//        if(dataContainer.isEmpty())
//        {
//            return Collections.emptyMap();
//        }
//        final Map<Long, com.alecmo.updatemgr.sql.DataModifier> modifiers = new HashMap<>();
//        for (Map.Entry<Long, Row> rowEntry : dataContainer.getRowsMap(table).entrySet())
//        {
//            final Row row = rowEntry.getValue();
//            final Long tableId = row.get(MAPUPDATEDATAMODIFIER.TABLE_ID);
//            final String className = row.get(MAPUPDATEDATAMODIFIER.CLASS);
//            final com.alecmo.updatemgr.sql.DataModifier formatter = (DataModifier) Class.forName(className).getConstructor().newInstance();
//            modifiers.put(tableId, formatter);
//        }
//        return modifiers;
//    }
//
//    private Map<String, Row> getRelatedTablePatternRow(Collection<String> tableNames)
//    {
//        final Table table = Table.get(DefaultTableDefinitions.SequencePatternTable.TABLENAME);
//        final DataContainer dataContainer = getRelatedTablesDataContainer(table, tableNames);
//        return dataContainer.getRows(table).collect(Collectors.toMap( row -> row.get(DefaultTableDefinitions.SequencePatternTable.IDENTITY_COLUMN),
//                row -> row));
//    }
//
//    private Map<String, Long> getRelatedTablePatternValue(Collection<String> tableNames)
//    {
//        final Table table = Table.get(DefaultTableDefinitions.SequencePatternTable.TABLENAME);
//        final DataContainer dataContainer = getRelatedTablesDataContainer(table, tableNames);
//        return dataContainer.getRows(table).collect(Collectors.toMap( row -> row.get(DefaultTableDefinitions.SequencePatternTable.IDENTITY_COLUMN),
//                row -> row.get(DefaultTableDefinitions.SequencePatternTable.COMMIT_COLUMN)));
//    }
//
//    private DataContainer getRelatedTablesDataContainer(Table table, Collection<String> tableNames)
//    {
//        final Set<String> allTables = new HashSet<>();
//        for (String tableName : tableNames)
//        {
//            allTables.add(tableName);
//            final Table child = Table.get(tableName);
//            final Map<String, FKDefinition> fkDefinitionMap = child.getTableDefinition().getFKDefinitions();
//            fkDefinitionMap.values().stream().map(FKDefinition::getReferenceTable).forEach(allTables::add);
//        }
//        final CriteriaBuilder<SQLCriteria> whereClause = new CriteriaBuilder<>(SQLCriteria.in(table.getColumn(DefaultTableDefinitions.SequencePatternTable.TABLE_COLUMN), allTables));
//        return DBAccessProvider.getDataStore().get(table, whereClause);
//    }
//
//    public static class PKValidatorImpl implements PKValidator
//    {
//        private final String pkPrefix;
//        public PKValidatorImpl(String pkPrefix)
//        {
//            this.pkPrefix = Objects.requireNonNull(pkPrefix);
//        }
//
//        @Override
//        public boolean isValid(String table, String column, String pattern)
//        {
//            return pattern.startsWith(table+":"+column+":"+pkPrefix+":");
//        }
//
//        @Override
//        public String validPkPattern(String table, String column)
//        {
//            return table+":"+column+":"+pkPrefix+":IDX";
//        }
//    }
//}
