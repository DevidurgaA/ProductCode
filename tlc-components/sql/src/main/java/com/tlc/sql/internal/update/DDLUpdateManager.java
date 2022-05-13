package com.tlc.sql.internal.update;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.meta.*;
import com.tlc.sql.internal.status.SQLErrorCodes;
import com.tlc.sql.update.ddl.AdvDDLAction;
import com.tlc.sql.update.ddl.DDLAction;
import com.tlc.sql.internal.update.impl.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Abishek
 * @version 1.0
 */
public class DDLUpdateManager
{
    private final AdminDataStore adminDataStore;
    public DDLUpdateManager(AdminDataStore adminDataStore)
    {
        this.adminDataStore = Objects.requireNonNull(adminDataStore);
    }

    public void doPreSchemaUpdate(Map<String, TableDefinition> currentTableDef, Map<String, TableDefinition> newTableDef)
    {
        final List<AdvDDLAction> tobeExecuted = getPreDDLActions(currentTableDef, newTableDef);
        if(!tobeExecuted.isEmpty())
        {
            adminDataStore.executeDDLActions(tobeExecuted);
        }
    }


    public void doPostSchemaUpdate(Map<String, TableDefinition> currentTableDef, Map<String, TableDefinition> newTableDef)
    {
        final List<DDLAction> tobeExecuted = getPostDDLActions(currentTableDef, newTableDef);
        if(!tobeExecuted.isEmpty())
        {
            adminDataStore.executeDDLActions(tobeExecuted);
        }
    }

    private List<AdvDDLAction> getPreDDLActions(Map<String, TableDefinition> oldTableDef, Map<String, TableDefinition> newTableDef)
    {
        final Set<String> tables = new HashSet<>(oldTableDef.keySet());
        tables.addAll(newTableDef.keySet());

        final NavigableMap<Integer, List<NavigableMap<ActionType.PreActionType, List<AdvDDLAction>>>> changes = new TreeMap<>();
        for (String table : tables)
        {
            final TableDefinition existingDef = oldTableDef.get(table);
            final TableDefinition newDef = newTableDef.get(table);
            final Integer order = newDef != null ? newDef.getSeqId() : existingDef.getSeqId();

            final NavigableMap<ActionType.PreActionType, List<AdvDDLAction>> actions = findPreActions(existingDef, newDef);
            if(!actions.isEmpty())
            {
                changes.computeIfAbsent(order, k -> new LinkedList<>()).add(actions);
            }
        }
        final List<AdvDDLAction> tobeExecuted = new LinkedList<>();
        for (List<NavigableMap<ActionType.PreActionType, List<AdvDDLAction>>> value : changes.values())
        {
            for (NavigableMap<ActionType.PreActionType, List<AdvDDLAction>> actions : value)
            {
                for (List<AdvDDLAction> ddlActions : actions.values())
                {
                    tobeExecuted.addAll(ddlActions);
                }
            }
        }
        return tobeExecuted;
    }

    private List<DDLAction> getPostDDLActions(Map<String, TableDefinition> oldTableDef, Map<String, TableDefinition> newTableDef)
    {
        final Set<String> tables = new HashSet<>(oldTableDef.keySet());
        tables.addAll(newTableDef.keySet());

        final NavigableMap<Integer, List<NavigableMap<ActionType.PostActionType, List<DDLAction>>>> changes = new TreeMap<>();
        for (String table : tables)
        {
            final TableDefinition existingDef = oldTableDef.get(table);
            final TableDefinition newDef = newTableDef.get(table);
            final Integer order = newDef != null ? newDef.getSeqId() : existingDef.getSeqId();

            final NavigableMap<ActionType.PostActionType, List<DDLAction>> actions = findPostActions(existingDef, newDef);
            if(!actions.isEmpty())
            {
                changes.computeIfAbsent(order, k -> new LinkedList<>()).add(actions);
            }
        }

        final List<DDLAction> tobeExecuted = new LinkedList<>();
        for (List<NavigableMap<ActionType.PostActionType, List<DDLAction>>> value : changes.descendingMap().values())
        {
            for (NavigableMap<ActionType.PostActionType, List<DDLAction>> actions : value)
            {
                for (List<DDLAction> ddlActions : actions.values())
                {
                    tobeExecuted.addAll(ddlActions);
                }
            }
        }
        return tobeExecuted;
    }

    private NavigableMap<ActionType.PreActionType, List<AdvDDLAction>> findPreActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final NavigableMap<ActionType.PreActionType, List<AdvDDLAction>> actions = new TreeMap<>();
        if (existingDef == null)
        {
            actions.put(ActionType.PreActionType.CREATE_TABLE, List.of(new CreateTable(newDef)));
        }
        else if (newDef != null)
        {
            if(isPKModified(existingDef, newDef))
            {
                throw ErrorCode.get(SQLErrorCodes.DB_UPGRADE_PK_UPDATE_NOT_ALLOWED);
            }
            final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> preColumnActions = getPreColumnActions(existingDef, newDef);
            actions.putAll(preColumnActions);

            final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> ukActions = getPreUKActions(existingDef, newDef);
            actions.putAll(ukActions);

            final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> idxActions = getIdxActions(existingDef, newDef);
            actions.putAll(idxActions);
        }
        return actions;
    }

    private boolean isPKModified(TableDefinition existingDef, TableDefinition newDef)
    {
        final PKDefinition existingXML = existingDef.getPkDefinition();
        final PKDefinition newXML = newDef.getPkDefinition();
        final ColumnDefinition newXMLColumn = existingDef.getColumnDefinition(newXML.geColumnName());
        return !existingXML.getKeyName().equals(newXML.getKeyName()) || !existingXML.geColumnName().equals(newXML.geColumnName())
                || !existingXML.getSequenceDefinition().getSequenceName().equals(newXML.getSequenceDefinition().getSequenceName())
                || newXMLColumn.getDataType() != DataType.BIGINT;
    }

    private NavigableMap<ActionType.PostActionType, List<DDLAction>> findPostActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final NavigableMap<ActionType.PostActionType, List<DDLAction>> actions = new TreeMap<>();
        if(newDef == null)
        {
            actions.put(ActionType.PostActionType.DROP_TABLE, List.of(new DropTable(existingDef)));
        }
        else if(existingDef != null)
        {
            final SortedMap<ActionType.PostActionType, List<DDLAction>> postColumnActions = getPostColumnActions(existingDef, newDef);
            actions.putAll(postColumnActions);

            final SortedMap<ActionType.PostActionType, List<DDLAction>> ukActions = getPostUKActions(existingDef, newDef);
            actions.putAll(ukActions);

            final SortedMap<ActionType.PostActionType, List<DDLAction>> fkActions = getPostFKActions(existingDef, newDef);
            actions.putAll(fkActions);
        }
        return actions;
    }

    private SortedMap<ActionType.PreActionType, List<AdvDDLAction>> getPreColumnActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> actions = new TreeMap<>();

        final Set<String> existingColumns = existingDef.copyNonPkColumns();
        for (String newColumn : newDef.copyNonPkColumns())
        {
            final ColumnDefinition newColumnDef = newDef.getColumnDefinition(newColumn);
            if(existingColumns.remove(newColumn))
            {
                final ColumnDefinition existingColumnDef = existingDef.getColumnDefinition(newColumn);
                final DataType existingDataType = existingColumnDef.getDataType();
                final DataType newDataType = newColumnDef.getDataType();

                if(existingDataType != newDataType)
                {
                    if(existingDataType.canConvertTo(newDataType))
                    {
                        actions.computeIfAbsent(ActionType.PreActionType.CHANGE_COLUMN_DATA_TYPE, k -> new LinkedList<>()).add(new UpdateColumnDataType(newDef, existingColumnDef, newColumnDef));
                    }
                    else
                    {
                        throw ErrorCode.get(SQLErrorCodes.DB_UPGRADE_COLUMN_DATA_TYPE_NOT_ALLOWED);
                    }
                }
                if(newColumnDef.getMaxLength() > existingColumnDef.getMaxLength())
                {
                    actions.computeIfAbsent(ActionType.PreActionType.INCREASE_COLUMN_LENGTH, k -> new LinkedList<>()).add(new IncreaseColumnLength(newDef, existingColumnDef, newColumnDef));
                }
                if(newColumnDef.isNullable() && !existingColumnDef.isNullable())
                {
                    actions.computeIfAbsent(ActionType.PreActionType.UPDATE_COLUMN_NULLABLE, k -> new LinkedList<>()).add(new UpdateColumnNullable(newDef, newColumnDef));
                }

                if (newColumnDef.getDataType() == DataType.DECIMAL) {
                    loadDecimalColumnAction(actions, newDef, newColumnDef, existingColumnDef);
                }
            }
            else
            {
                actions.computeIfAbsent(ActionType.PreActionType.CREATE_COLUMN_NULLABLE, k -> new LinkedList<>()).add(new AddColumnNullable(newDef, newColumnDef));
            }
        }
        for (String existingColumn : existingColumns)
        {
            final ColumnDefinition existingColumnDef = existingDef.getColumnDefinition(existingColumn);
            if(!existingColumnDef.isNullable())
            {
                actions.computeIfAbsent(ActionType.PreActionType.UPDATE_COLUMN_NULLABLE, k -> new LinkedList<>()).add(new UpdateColumnNullable(existingDef, existingColumnDef));
            }
        }
        return actions;
    }

    /**
     * Loads the decimal data type changes
     *
     * @param actions Pre column actions container
     * @param newDef Table definition
     * @param newColumnDef New column definition
     * @param existingColumnDef Existing column definition
     */
    private void loadDecimalColumnAction(final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> actions,
                                       final TableDefinition newDef, final ColumnDefinition newColumnDef,
                                       final ColumnDefinition existingColumnDef) {
        if (newColumnDef.getPrecision() < existingColumnDef.getPrecision()) {
            throw ErrorCode.get(SQLErrorCodes.DB_INVALID_NUMERIC_PRECISION);
        }

        if (newColumnDef.getScale() < existingColumnDef.getScale()) {
            throw ErrorCode.get(SQLErrorCodes.DB_INVALID_NUMERIC_SCALE);
        }

        if (newColumnDef.getPrecision() > existingColumnDef.getPrecision() ||
                newColumnDef.getScale() > existingColumnDef.getScale()) {
            actions.computeIfAbsent(ActionType.PreActionType.INCREASE_DECIMAL_COLUMN_WEIGHT,
                    k -> new LinkedList<>()).add(new IncreaseDecimalColumnWeight(newDef, newColumnDef,
                    existingColumnDef));
        }
    }

    private SortedMap<ActionType.PostActionType, List<DDLAction>> getPostColumnActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PostActionType, List<DDLAction>> actions = new TreeMap<>();
        final Set<String> toBeCreated = newDef.copyNonPkColumns();
        for (String existingColumn : existingDef.copyNonPkColumns())
        {
            final ColumnDefinition existingColumnDef = existingDef.getColumnDefinition(existingColumn);
            if(toBeCreated.remove(existingColumn))
            {
                final ColumnDefinition newColumnDef = newDef.getColumnDefinition(existingColumn);
                if(existingColumnDef.isNullable() && !newColumnDef.isNullable())
                {
                    actions.computeIfAbsent(ActionType.PostActionType.UPDATE_COLUMN_NOT_NULLABLE, k -> new LinkedList<>()).add(new UpdateColumnNotNullable(newDef, newColumnDef));
                }
            }
            else
            {
                actions.computeIfAbsent(ActionType.PostActionType.DELETE_COLUMN, k -> new LinkedList<>()).add(new DropColumn(existingDef, existingColumnDef));
            }
        }
        for (String newColumn : toBeCreated)
        {
            final ColumnDefinition newColumnDef = newDef.getColumnDefinition(newColumn);
            if(!newColumnDef.isNullable())
            {
                actions.computeIfAbsent(ActionType.PostActionType.UPDATE_COLUMN_NOT_NULLABLE, k -> new LinkedList<>()).add(new UpdateColumnNotNullable(newDef, newColumnDef));
            }
        }
        return actions;
    }

    private SortedMap<ActionType.PreActionType, List<AdvDDLAction>> getPreUKActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> actions = new TreeMap<>();
        final Map<String, UKDefinition> existingUK = existingDef.getUKDefinitions();
        final Map<String, UKDefinition> newUK = newDef.getUKDefinitions();

        for (Map.Entry<String, UKDefinition> entry : existingUK.entrySet())
        {
            if(!newUK.containsKey(entry.getKey()))
            {
                actions.computeIfAbsent(ActionType.PreActionType.DROP_UNIQUE_KEY, k -> new LinkedList<>()).add(new DropUniqueKey(existingDef, entry.getValue()));
            }
        }
        return actions;
    }

    private SortedMap<ActionType.PostActionType, List<DDLAction>> getPostUKActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PostActionType, List<DDLAction>> actions = new TreeMap<>();
        final Map<String, UKDefinition> existingUKs = existingDef.getUKDefinitions();
        final Map<String, UKDefinition> newUKs = newDef.getUKDefinitions();
        for (Map.Entry<String, UKDefinition> entry : newUKs.entrySet())
        {
            final UKDefinition newUK = entry.getValue();
            final UKDefinition existingUK = existingUKs.get(entry.getKey());
            if(existingUK == null)
            {
                actions.computeIfAbsent(ActionType.PostActionType.CREATE_UNIQUE_KEY, k -> new LinkedList<>()).add(new CreateUniqueKey(newDef, newUK));
            }
            else
            {
                final Set<String> existingColumns = new HashSet<>(existingUK.getColumns());
                final AtomicBoolean update = new AtomicBoolean(false);
                for(String newColumn : newUK.getColumns())
                {
                    if(!existingColumns.remove(newColumn))
                    {
                        update.set(true);
                        break;
                    }
                }
                update.compareAndSet(false, !existingColumns.isEmpty());

                if(update.get())
                {
                    actions.computeIfAbsent(ActionType.PostActionType.UPDATE_UNIQUE_KEY, k -> new LinkedList<>()).add(new UpdateUniqueKey(newDef, existingUK, newUK));
                }
            }
        }
        return actions;
    }

    private SortedMap<ActionType.PreActionType, List<AdvDDLAction>> getIdxActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PreActionType, List<AdvDDLAction>> actions = new TreeMap<>();

        final Map<String, IndexDefinition> existingIndexes = new HashMap<>(existingDef.getIndexDefinitions());
        final Map<String, IndexDefinition> newIndexes = newDef.getIndexDefinitions();
        for (Map.Entry<String, IndexDefinition> entry : newIndexes.entrySet())
        {
            final String idxName = entry.getKey();
            final IndexDefinition newIdx = entry.getValue();
            final IndexDefinition existingIdx = existingIndexes.remove(idxName);
            if(existingIdx == null)
            {
                actions.computeIfAbsent(ActionType.PreActionType.CREATE_INDEX, k -> new LinkedList<>()).add(new CreateIndex(newDef, newIdx));
            }
            else
            {
                final Set<String> existingColumns = new HashSet<>(existingIdx.getColumns());
                final Set<String> newColumns = new HashSet<>(newIdx.getColumns());
                existingColumns.removeAll(newColumns);
                if(!existingColumns.isEmpty())
                {
                    actions.computeIfAbsent(ActionType.PreActionType.UPDATE_INDEX, k -> new LinkedList<>()).add(new UpdateIndex(newDef, existingIdx, newIdx));
                }
            }
        }
        if(!existingIndexes.isEmpty())
        {
            final List<AdvDDLAction> deleteActions = actions.computeIfAbsent(ActionType.PreActionType.DELETE_INDEX, k -> new LinkedList<>());
            for (IndexDefinition deleted : existingIndexes.values())
            {
                deleteActions.add(new DropIndex(existingDef, deleted));
            }
        }
        return actions;
    }


    private SortedMap<ActionType.PostActionType, List<DDLAction>> getPostFKActions(TableDefinition existingDef, TableDefinition newDef)
    {
        final SortedMap<ActionType.PostActionType, List<DDLAction>> actions = new TreeMap<>();
        final Map<String, FKDefinition> newFKs = newDef.getFKDefinitions();
        final Map<String, FKDefinition> existingFKs = new HashMap<>(existingDef.getFKDefinitions());
        for (Map.Entry<String, FKDefinition> entry : newFKs.entrySet())
        {
            final FKDefinition existingFk = existingFKs.remove(entry.getKey());
            final FKDefinition newFk = entry.getValue();
            if(existingFk == null)
            {
                actions.computeIfAbsent(ActionType.PostActionType.CREATE_FOREIGN_KEY, k -> new LinkedList<>()).add(new AddForeignKey(newDef, newFk));
            }
            else if(!existingFk.getLocalTable().equals(newFk.getLocalTable()) || !existingFk.getReferenceTable().equals(newFk.getReferenceTable())
                    || !existingFk.getLocal().getColumnName().equals(newFk.getLocal().getColumnName()) || !existingFk.getRemote().getColumnName().equals(newFk.getRemote().getColumnName())
                        || existingFk.getConstraint() != newFk.getConstraint())
            {
                actions.computeIfAbsent(ActionType.PostActionType.UPDATE_FOREIGN_KEY, k -> new LinkedList<>()).add(new UpdateForeignKey(newDef, existingFk, newFk));
            }
        }
        if(!existingFKs.isEmpty())
        {
            for (FKDefinition value : existingFKs.values())
            {
                actions.computeIfAbsent(ActionType.PostActionType.DROP_FOREIGN_KEY, k -> new LinkedList<>()).add(new DropForeignKey(existingDef, value));
            }
        }
        return actions;
    }

}
