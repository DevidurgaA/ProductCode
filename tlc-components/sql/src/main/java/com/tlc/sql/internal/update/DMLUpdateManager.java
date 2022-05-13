package com.tlc.sql.internal.update;

import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.meta.ColumnDefinition;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.internal.meta.MetaDataHandler;
import com.tlc.sql.internal.parser.DynamicPatternResolver;
import com.tlc.sql.internal.parser.PatternResolver;
import com.tlc.sql.internal.parser.XmlToDCConverter;
import com.tlc.sql.resource.FGSEQUENCEPATTERN;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DMLUpdateManager
{
    /*
            DO NOT CHANGE PK PATTERN FOR ANY ALREADY ASSIGNED VALUES

            ID   NAME(UK)
            P:1   TEST
            P:2   TEST1

            to

            ID   NAME(UK)
            P:1   TEST1
            P:2   TEST

            Will results in error - Always follow the order

            Proper way to do this,

            ID   NAME(UK)
            P:3   TEST1
            P:4   TEST

     */

    private final AdminDataStore adminDataStore;
    private final MetaDataHandler metaDataHandler;
    public DMLUpdateManager(MetaDataHandler metaDataHandler, AdminDataStore adminDataStore)
    {
        this.metaDataHandler = Objects.requireNonNull(metaDataHandler);
        this.adminDataStore = Objects.requireNonNull(adminDataStore);
    }

    public void handleDMLCorrection(SortedMap<Integer, DataCorrector> dataCorrectors)
    {
        for (DataCorrector corrector : dataCorrectors.values())
        {
            corrector.doCorrection();
        }
    }

    public DataContainer doDMLUpdate(List<File> current, List<File> newFiles, Set<String> ignoreTables, Map<String, String> dynamicData)
    {
        final PatternResolver patternResolver = new DynamicPatternResolver(adminDataStore);
        final DataContainer currentContainer = metaDataHandler.fetchDataContainer(current, patternResolver, dynamicData, XmlToDCConverter.Type.FETCH);
        final DataContainer databaseDataContainer = getDataFromDatabase(currentContainer, ignoreTables);
        final DataContainer newContainer = metaDataHandler.fetchDataContainer(newFiles, patternResolver, dynamicData,XmlToDCConverter.Type.MIXED);

        final DataContainer tobeUpdated = DataContainer.create();
        final List<DeleteQuery> seqQueries = new LinkedList<>();

        loadChanges(tobeUpdated, seqQueries, currentContainer, databaseDataContainer, newContainer, ignoreTables);

        adminDataStore.commitChanges(tobeUpdated);
        adminDataStore.delete(seqQueries);

        return tobeUpdated;
    }

    public DataContainer getDataFromDatabase(DataContainer existingXmlContainer, Set<String> ignoreTables)
    {
        final DataContainer dataContainer = DataContainer.create();
        final Set<Table> tables = existingXmlContainer.getTables();
        for (Table table : tables)
        {
            if(ignoreTables.contains(table.getName()))
            {
                continue;
            }
            final Set<Long> pkIds = existingXmlContainer.getRowsMap(table).keySet();
            if(!pkIds.isEmpty())
            {
                final DataContainer tableData = adminDataStore.get(table, pkIds);
                dataContainer.append(tableData);
            }
        }
        return dataContainer;
    }

    public void loadChanges(DataContainer container, List<DeleteQuery> seqQueries, DataContainer cC, DataContainer dC, DataContainer nC, Set<String> ignore)
    {
        final Set<Table> newTables = new HashSet<>(nC.getTables());
        final Table sequenceTable = Table.get(FGSEQUENCEPATTERN.TABLE);
        final Column sequenceTableNameColumn = sequenceTable.getColumn(FGSEQUENCEPATTERN.TABLE_NAME);
        nC.indexRows(sequenceTableNameColumn);

        for (Table oldTable : cC.getTables())
        {
            final String tableName = oldTable.getName();
            if(ignore.contains(tableName))
            {
                continue;
            }
            final Map<Long, Row> currentXML = cC.getRowsMap(oldTable);
            final Map<Long, Row> currentDB = dC.getRowsMap(oldTable);

            if(newTables.remove(oldTable))
            {
                final Map<Long, Row> newXML = nC.getRowsMap(oldTable);
                final Set<Long> existingRowIds = new HashSet<>(currentXML.keySet());

                final Map<Long, Row> sequenceRows = new HashMap<>();
                nC.getIndexedRows(sequenceTableNameColumn, tableName).forEach( row ->
                {
                    final Long committedValue = row.get(FGSEQUENCEPATTERN.COMMIT_VALUE);
                    sequenceRows.put(committedValue, row);
                });

                for (Map.Entry<Long, Row> entry : newXML.entrySet())
                {
                    final Long id = entry.getKey();
                    final Row newXMLRow = entry.getValue();
                    if(existingRowIds.remove(id))
                    {
                        final Row currentXMLRow = currentXML.get(id);
                        final Row currentDBRow = currentDB.get(id);
                        if(compareAndUpdate(currentXMLRow, currentDBRow, newXMLRow))
                        {
                            container.storeRow(newXMLRow);
                            container.updateRow(newXMLRow);
                        }
                    }
                    else
                    {
                        final Row sequenceRow = sequenceRows.get(id);
                        container.addNewRow(sequenceRow);
                        container.addNewRow(newXMLRow);
                    }
                }

                if(!existingRowIds.isEmpty())
                {
                    for (Long existingRowId : existingRowIds)
                    {
                        final Row currentXMLRow = currentXML.get(existingRowId);
                        container.storeRow(currentXMLRow);
                        container.deleteRow(currentXMLRow);
                    }
                    final DeleteQuery deleteQuery = DeleteQuery.get(sequenceTable);
                    final WhereClause whereClause = new WhereClause(Criteria.eq(sequenceTable.getColumn(FGSEQUENCEPATTERN.TABLE_NAME), tableName));
                    deleteQuery.setWhereClause(whereClause.and(Criteria.in(sequenceTable.getColumn(FGSEQUENCEPATTERN.COMMIT_VALUE), existingRowIds)));
                    seqQueries.add(deleteQuery);
                }
            }
            else
            {
                //TODO check for drop table and leave without changes -- oldTable order problem
                for (Row row : currentDB.values())
                {
                    container.storeRow(row);
                    container.deleteRow(row);
                }
                final DeleteQuery deleteQuery = DeleteQuery.get(sequenceTable);
                final WhereClause whereClause = new WhereClause(Criteria.eq(sequenceTable.getColumn(FGSEQUENCEPATTERN.TABLE_NAME), tableName));
                deleteQuery.setWhereClause(whereClause.and(Criteria.in(sequenceTable.getColumn(FGSEQUENCEPATTERN.COMMIT_VALUE), currentDB.keySet())));
                seqQueries.add(deleteQuery);
            }
        }
        for (Table newTable : newTables)
        {
            final Map<Long, Row> sequenceRows = new HashMap<>();
            nC.getIndexedRows(sequenceTableNameColumn, newTable.getName()).forEach( row ->
            {
                final Long committedValue = row.get(FGSEQUENCEPATTERN.COMMIT_VALUE);
                sequenceRows.put(committedValue, row);
            });
            final Map<Long, Row> newXML = nC.getRowsMap(newTable);
            for (Map.Entry<Long, Row> entry : newXML.entrySet())
            {
                final Row sequenceRow = sequenceRows.get(entry.getKey());
                container.addNewRow(sequenceRow);
                container.addNewRow(entry.getValue());
            }
        }
    }

    private boolean compareAndUpdate(Row currentXMLRow, Row dbRow, Row newXMLRow)
    {
        final Map<String, ColumnDefinition> currentColumnDefinitions = currentXMLRow.getTable().getTableDefinition().getColumnDefinition();
        final Map<String, ColumnDefinition> newColumnDefinitions = newXMLRow.getTable().getTableDefinition().getColumnDefinition();

        final AtomicBoolean status = new AtomicBoolean(false);
        final Set<String> currentColumns = new HashSet<>(currentColumnDefinitions.keySet());

        for (ColumnDefinition newColumnDef : newColumnDefinitions.values())
        {
            final String columnName = newColumnDef.getColumnName();
            final Object newXMLData = newXMLRow.get(columnName);
            if(currentColumns.remove(columnName))
            {
                final DataType columnDataType = newColumnDef.getDataType();
                final Object currentXMLData = currentXMLRow.get(columnName);
                final Object dbData = dbRow.get(columnName);
                if(columnDataType.isEqual(currentXMLData, dbData))
                {
                    if(!columnDataType.isEqual(newXMLData, dbData))
                    {
                        status.compareAndSet(false, true);
                    }
                }
                else
                {
                    newXMLRow.set(columnName, dbData);
                }
            }
            else
            {
                status.compareAndSet(false, true);
            }
        }
        return status.get();
    }

}
