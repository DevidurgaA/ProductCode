package com.tlc.crm.common.config;

import com.tlc.commons.code.ErrorCode;
import com.tlc.crm.common.internal.resource.CommonErrorCodes;
import com.tlc.sql.SQLAccess;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.CountQuery;
import com.tlc.sql.api.dml.Criteria;
import com.tlc.sql.api.dml.SelectQuery;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.sql.api.meta.ImmutableColumns;
import com.tlc.validator.TlcModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class AbstractConfigManager<T extends TlcModel> implements ConfigManager<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final OrgDataStore orgDataStore(final Long orgId) {
        return SQLAccess.get().getOrgDataStore(orgId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void loadRowIntoContainer(final T model, final DataContainer container) {
        final Row existingRow = model.id() == null ? null : fetchRecord(model);

        loadRowIntoContainer(model, existingRow, container);
    }

    /**
     * {@inheritDoc}
     */
    public void loadRowIntoContainer(final T model, final Row existingRow, final DataContainer container) {

        if (null != model.id() && null == existingRow) {
            throw ErrorCode.get(CommonErrorCodes.ENTITY_NOT_FOUND);
        }

        loadRowIntoContainer(null, model, existingRow, container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowsIntoContainer(final Collection<T> models, final DataContainer dataContainer) {
        loadRowsIntoContainer(null, models, dataContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowsIntoContainer(final TlcModel relatedEntity, Collection<T> models, final DataContainer dataContainer) {
        final T firstModel = models.stream().findFirst().get();
        final DataContainer existingRecordsContainer = fetchRecords(firstModel.orgId(), models);
        final Map<Long, Row> rowMap = existingRecordsContainer.getRowsMap(getTable());

        models.stream().forEach(model -> {
            final Row existingRow = null == model.id() ? null : rowMap.get(model.id());

            if (null != model.id() && null == existingRow) {
                throw ErrorCode.get(CommonErrorCodes.ENTITY_NOT_FOUND);
            }

            loadRowIntoContainer(relatedEntity, model, existingRow, dataContainer);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> convertRows(final Collection<Row> rows) {
        return convertRows(rows.stream());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> convertRows(final Stream<Row> rows) {
        return rows.map(this::convertRowToModel).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final T model) {
        return exists(model.orgId(), model.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final Long orgId, final Long id) {
        final Table table = getTable();
        final CountQuery countQuery = CountQuery.get(table);
        final WhereClause whereClause = new WhereClause(Criteria.eq(table.getPKColumn(), id));

        countQuery.setWhereClause(whereClause);

        return orgDataStore(orgId).get(countQuery) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(final Long orgId, final Long id) {
        final Row row = orgDataStore(orgId).get(getTable(), id);

        return null == row ? null : convertRowToModel(row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> get(final Long orgId, final Collection<Long> ids) {
        final DataContainer dataContainer = fetchRecordsByIds(orgId, ids);

        return null == dataContainer ? List.of() : convertRows(dataContainer.getRows(getTable()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> getByOrgId(final Long id) {
        final Table table = getTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(table.getColumn(ImmutableColumns.ORG_ID.getName()), id));
        final DataContainer dataContainer = orgDataStore(id).get(table, whereClause);

        return convertRows(dataContainer.getRowsMap(table).values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row fetchRecord(final T model) {
        return orgDataStore(model.orgId()).get(getTable(), model.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataContainer fetchRecords(final Long orgId, final Collection<T> models) {
        final List<Long> ids = models.stream().filter(model -> null != model.id()).map(TlcModel::id).collect(Collectors.toList());

        return fetchRecordsByIds(orgId, ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataContainer fetchRecordsByIds(final Long orgId, final Collection<Long> ids) {
        return null == ids || ids.isEmpty() ? DataContainer.create() : orgDataStore(orgId).get(getTable(), ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataContainer fetchRecords(final Long orgId, SelectQuery selectQuery) {
        return orgDataStore(orgId).get(selectQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T create(final T model) {
        create(model.orgId(), List.of(model));

        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> create(final Long orgId, final Collection<T> models) {
        final Map<String, Object> response = new HashMap<>();
        final List<Long> createdRecords = new ArrayList<>();
        final List<Integer> failedEntriesIndex = new ArrayList<>();

        int index = 0;
        for (final Iterator<T> iterator = models.iterator(); iterator.hasNext(); index++) {
            final T model = iterator.next();

            try {
                final DataContainer dataContainer = DataContainer.create();

                loadRowIntoContainer(model, dataContainer);
                orgDataStore(orgId).commitChanges(dataContainer);
                createdRecords.add(model.id());
            } catch (ErrorCode e) {
                failedEntriesIndex.add(index);
            }
        }

        response.put("createdRecords", createdRecords);
        response.put("failedRecordsIndex", failedEntriesIndex);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T update(final T model) {
        update(model.orgId(), List.of(model));
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> update(final Long orgId, final Collection<T> models) {
        final OrgDataStore orgDataStore = orgDataStore(orgId);
        final Map<Long, Row> rowMap = fetchRecords(orgId, models).getRowsMap(getTable());
        final Map<String, Object> response = new HashMap<>();
        final List<Long> updatedRecords = new ArrayList<>();
        final List<Map<String, Object>> failedEntities = new ArrayList<>();

        models.stream().forEach(model -> {
            try {
                final Row existingRow = rowMap.get(model.id());

                if (null != existingRow) {
                    final DataContainer dataContainer = DataContainer.create();

                    loadRowIntoContainer(model, existingRow, dataContainer);
                    orgDataStore.commitChanges(dataContainer);
                    updatedRecords.add(model.id());
                } else {
                    throw ErrorCode.get(CommonErrorCodes.ENTITY_NOT_FOUND);
                }
            } catch (ErrorCode e) {
                final Map<String, Object> entity = new HashMap<>();

                entity.put("id", model.id());
                entity.put("reason", e.getMessage());
                failedEntities.add(entity);
            }
        });

        response.put("updatedRecords", updatedRecords);
        response.put("failedRecords", failedEntities);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final T model) {
        delete(model.orgId(), List.of(model));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Long orgId, final Collection<T> models) {
        orgDataStore(orgId).delete(getTable(), models.stream().map(TlcModel::id).collect(Collectors.toUnmodifiableSet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final DataContainer dataContainer, final Stream<Row> rows) {
        rows.forEach(row -> dataContainer.deleteRow(row));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Long orgId, final DataContainer dataContainer, final WhereClause whereClause) {
        delete(orgId, dataContainer, getTable(), whereClause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Long orgId, final DataContainer dataContainer, final Table table, final WhereClause whereClause) {
        final DataContainer rowsContainer = orgDataStore(orgId).get(table, whereClause);

        if (null != rowsContainer) {
            delete(dataContainer, rowsContainer.getRows(table));
        }
    }

}
