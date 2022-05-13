package com.tlc.crm.common.action.secure.list;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheConfig;
import com.tlc.cache.CacheManager;
import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.crm.common.sql.resource.*;
import com.tlc.i18n.I18nKey;
import com.tlc.sql.SQLAccess;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.OrgDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Abishek
 * @version 1.0
 */
public class StructureHandler
{
    private final Cache<String, ListStructure> sqlStructureCache;
    private static final Logger LOGGER = LoggerFactory.getLogger(StructureHandler.class.getName());
    private static class Helper
    {
        private static final StructureHandler INSTANCE = new StructureHandler();
    }

    public static StructureHandler get()
    {
        return Helper.INSTANCE;
    }

    private StructureHandler()
    {
        final CacheConfig<String, ListStructure> config = new CacheConfig<>();
        config.setExpireAfterAccess(TimeUnit.MINUTES.toMillis(10));
        this.sqlStructureCache = CacheManager.getInstance().createCache(config);
    }

    public void clearCache()
    {
        sqlStructureCache.clear();
    }

    public ListStructure getStructure(Long userId, String provider)
    {
        return sqlStructureCache.computeIfAbsent(provider, k-> fetchStructure(k, userId));
    }

    public List<Field> fetchConfiguredFields(Long userId, String provider)
    {
        final ListStructure listStructure = getStructure(userId, provider);
        final Long providerId = listStructure.getProviderId();

        final Table fieldsTable = Table.get(MCMFIELDS.TABLE);
        final Table configuredFieldsTable = Table.get(OMCMCONFIGUREDFIELDS.TABLE);

        final SelectQuery sQuery = SelectQuery.get(fieldsTable);
        final List<JoinClause> innerJoins = DmlUtil.getJoins(fieldsTable, configuredFieldsTable);
        sQuery.addJoinClause(innerJoins);
        sQuery.addSelectClause(fieldsTable.getColumn(MCMFIELDS.NAME));
        sQuery.addOrderByClause(new OrderByClause(configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.ORDER), OrderByClause.OrderType.ASCENDING));
        WhereClause whereClause = new WhereClause(Criteria.eq(configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.PROVIDER_ID), providerId));
        whereClause = whereClause.and(Criteria.eq(configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.USER_ID), userId));
        sQuery.setWhereClause(whereClause);

        final OrgDataStore orgDataStore = SQLAccess.get().getOrgDataStore(userId);
        final DataContainer dataContainer = orgDataStore.get(sQuery);
        if(dataContainer.isEmpty())
        {
            LOGGER.debug("Fields not configured, loading default fields for provider {}", provider);
            return listStructure.getFields().stream().filter( f -> !f.hidden()).collect(Collectors.toList());
        }
        else
        {
            LOGGER.debug("Fields configured, loading configured fields for provider {}", provider);
            return dataContainer.getRows(fieldsTable).map( row -> listStructure.getField(row.get(MCMFIELDS.NAME))).collect(Collectors.toList());
        }
    }

    public void updateConfiguredFields(Long userId, Long providerId, List<Field> fields)
    {
        final Table configuredFieldsTable = Table.get(OMCMCONFIGUREDFIELDS.TABLE);

        final SelectQuery sQuery = SelectQuery.get(configuredFieldsTable);
        sQuery.addSelectClause(configuredFieldsTable);
        WhereClause whereClause = new WhereClause(Criteria.eq(configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.PROVIDER_ID), providerId));
        whereClause = whereClause.and(Criteria.eq(configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.USER_ID), userId));
        sQuery.setWhereClause(whereClause);

        final OrgDataStore orgDataStore = SQLAccess.get().getOrgDataStore(userId);
        final DataContainer dataContainer = orgDataStore.get(sQuery);

        if(dataContainer.isEmpty())
        {
            LOGGER.debug("Fields not configured, Configuring fields for provider {}", providerId);
            final AtomicInteger order = new AtomicInteger(0);
            for (Field field : fields)
            {
                if(!field.hidden())
                {
                    final Row row = new Row(configuredFieldsTable);
                    row.set(OMCMCONFIGUREDFIELDS.USER_ID, userId);
                    row.set(OMCMCONFIGUREDFIELDS.PROVIDER_ID, providerId);
                    row.set(OMCMCONFIGUREDFIELDS.FIELD_ID, field.id());
                    row.set(OMCMCONFIGUREDFIELDS.ORDER, order.incrementAndGet());
                    dataContainer.addNewRow(row);
                }
            }
        }
        else
        {
            LOGGER.debug("Fields already configured, Reconfiguring fields for provider {}", providerId);
            final Column fieldIdColumn = configuredFieldsTable.getColumn(OMCMCONFIGUREDFIELDS.FIELD_ID);
            dataContainer.indexRows(fieldIdColumn);

            final Set<Long> currentIds = dataContainer.getRows(configuredFieldsTable)
                    .map( row -> (Long)row.get(OMCMCONFIGUREDFIELDS.FIELD_ID)).collect(Collectors.toSet());

            final AtomicInteger order = new AtomicInteger(0);
            for (Field field : fields)
            {
                final Long fieldId = field.id();
                if(currentIds.remove(fieldId))
                {
                    final Row update = dataContainer.getIndexedRow(fieldIdColumn, fieldId);
                    update.set(OMCMCONFIGUREDFIELDS.ORDER, order.incrementAndGet());
                    dataContainer.updateRow(update);
                }
                else
                {
                    final Row newRow = new Row(configuredFieldsTable);
                    newRow.set(OMCMCONFIGUREDFIELDS.USER_ID, userId);
                    newRow.set(OMCMCONFIGUREDFIELDS.PROVIDER_ID, providerId);
                    newRow.set(OMCMCONFIGUREDFIELDS.FIELD_ID, field.id());
                    newRow.set(OMCMCONFIGUREDFIELDS.ORDER, order.incrementAndGet());
                    dataContainer.addNewRow(newRow);
                }

            }
            for (Long tobeDeleted : currentIds)
            {
                final Row delete = dataContainer.getIndexedRow(fieldIdColumn, tobeDeleted);
                dataContainer.deleteRow(delete);
            }
        }

        orgDataStore.commitChanges(dataContainer);
    }

    private ListStructure fetchStructure(String providerName, Long userId)
    {
        LOGGER.debug("Loading Structure for provider {}", providerName);
        final List<Field> fieldList = new ArrayList<>();

        final Table providerTable = Table.get(MCMDATAPROVIDER.TABLE);
        final Table fieldsTable = Table.get(MCMFIELDS.TABLE);
        final Table transformerTable = Table.get(MCMDATATRANSFORMERS.TABLE);
        final Table dataSourceTable = Table.get(MCMFIELDSQLSOURCE.TABLE);
        final Table dataSourceSQLTable = Table.get(MCMFIELDSQLTABLE.TABLE);

        final Column fieldIdColumn = dataSourceTable.getColumn(MCMFIELDSQLSOURCE.FIELD_ID);

        final SelectQuery sQuery = SelectQuery.get(providerTable);
        final List<JoinClause> innerJoins = DmlUtil.getJoins(providerTable, fieldsTable, dataSourceTable, dataSourceSQLTable);
        sQuery.addJoinClause(innerJoins);
        sQuery.addJoinClause(new JoinClause(fieldsTable.getColumn(MCMFIELDS.TRANSFORMER_ID), transformerTable.getColumn(MCMDATATRANSFORMERS.ID), JoinClause.JoinType.LEFT));
        sQuery.addSelectClause(providerTable, fieldsTable, dataSourceTable, dataSourceSQLTable, transformerTable);
        sQuery.addOrderByClause(new OrderByClause(fieldsTable.getColumn(MCMFIELDS.ORDER), OrderByClause.OrderType.ASCENDING));
        sQuery.setWhereClause(new WhereClause(Criteria.eq(providerTable.getColumn(MCMDATAPROVIDER.NAME), providerName)));

        final OrgDataStore orgDataStore = SQLAccess.get().getOrgDataStore(userId);
        final DataContainer dataContainer = orgDataStore.get(sQuery);
        final Row providerRow = dataContainer.getRow(providerTable);

        if(providerRow == null)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Provider Not Found!!!!!");
        }

        final Long providerId = providerRow.get(MCMDATAPROVIDER.ID);
        dataContainer.indexRows(fieldIdColumn);

        dataContainer.getRows(fieldsTable).forEach( fieldRow ->
        {
            final Long fieldId = fieldRow.get(MCMFIELDS.ID);
            final String fieldName = fieldRow.get(MCMFIELDS.NAME);
            final I18nKey displayName = fieldRow.get(MCMFIELDS.DISPLAY_NAME);
            final Boolean primary = fieldRow.get(MCMFIELDS.PRIMARY);

            final int sortFlag = fieldRow.get(MCMFIELDS.SORT);
            final Boolean searchable = fieldRow.get(MCMFIELDS.SEARCHABLE);
            final Boolean hidden = fieldRow.get(MCMFIELDS.HIDDEN);
            final Long transformerId = fieldRow.get(MCMFIELDS.TRANSFORMER_ID);
            final String fieldConfigInfo = fieldRow.get(MCMFIELDS.CONFIG_INFO);

            final Row dataSourceRow = dataContainer.getIndexedRow(fieldIdColumn, fieldId);
            final Long tableId = dataSourceRow.get(MCMFIELDSQLSOURCE.TABLE_ID);
            final String columnName = dataSourceRow.get(MCMFIELDSQLSOURCE.COLUMN);
            final Integer column_type = dataSourceRow.get(MCMFIELDSQLSOURCE.TYPE);
            final Integer flag = dataSourceRow.get(MCMFIELDSQLSOURCE.FLAG);

            final Row tableRow = dataContainer.getRow(dataSourceSQLTable, tableId);
            final String tableName = tableRow.get(MCMFIELDSQLTABLE.TABLE_NAME);
            final FieldSource fieldSource = new FieldSource(tableName, columnName, column_type, flag);

            final Transformer transformer;
            if(transformerId != null)
            {
                final Row row = dataContainer.getRow(transformerTable, transformerId);

                final String className = row.get(MCMDATATRANSFORMERS.CLASS);
                final String method = row.get(MCMDATATRANSFORMERS.METHOD);
                final String arguments = row.get(MCMDATATRANSFORMERS.ARGUMENTS);
                final String[] argumentsArray = arguments == null ? null : arguments.split(",");
                try
                {
                    final Method discovered = Class.forName(className).getMethod(method, Object.class, Map.class, String[].class);
                    transformer = (data, map) ->
                    {
                        try
                        {
                            return discovered.invoke(null, data, map, argumentsArray);  //TODO
                        }
                        catch (Exception exp)
                        {
                            LOGGER.debug("Unable to transform data, Reason", exp);
                        }
                        return data;
                    };
                }
                catch (Exception exp)
                {
                    throw ErrorCode.get(ErrorCodes.INVALID_DATA);
                }
            }
            else
            {
                transformer = null;
            }

            fieldList.add(new Field(fieldId, fieldName, displayName, primary, sortFlag, searchable,
                    hidden, fieldConfigInfo, fieldSource, transformer));
        });

        return new ListStructure(providerId, fieldList);
    }
}