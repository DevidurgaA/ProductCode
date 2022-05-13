package com.tlc.crm.common.config;

import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.SelectQuery;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.validator.TlcModel;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public interface ConfigManager<T extends TlcModel> {

    /**
     * <p>
     * Fetches the table of the manager
     * </p>
     *
     * @return {@link Table} definitions of a manager
     */
    Table getTable();

    /**
     * <p>
     * Returns {@link OrgDataStore} of an organization
     * </p>
     *
     * @param orgId
     * @return {@link OrgDataStore}
     */
    OrgDataStore orgDataStore(final Long orgId);

    /**
     * <p>
     * Loads the given record into the container
     * </p>
     *
     * @param model
     * @param container
     */
    void loadRowIntoContainer(final T model, final DataContainer container);

    /**
     * <p>
     * Loads the given record into the container
     * </p>
     *
     * @param relatedEntity
     * @param model
     * @param dataContainer
     */
    void loadRowIntoContainer(final TlcModel relatedEntity, final T model, final Row existingRow,
                              final DataContainer dataContainer);

    /**
     * <p>
     * Loads the row objects corresponding to the {@link TlcModel} into the container
     * </p>
     *
     * @param relatedEntity
     * @param models
     * @param dataContainer
     */
    void loadRowsIntoContainer(final TlcModel relatedEntity, Collection<T> models, final DataContainer dataContainer);

    /**
     * <p>
     * Loads the row objects corresponding to the {@link TlcModel} into the container
     * </p>
     *
     * @param models
     * @param dataContainer
     */
    void loadRowsIntoContainer(final Collection<T> models, final DataContainer dataContainer);

    /**
     * <p>
     * Converts {@link Row} into the subclass of {@link TlcModel} object
     * </p>
     *
     * @param row
     * @return Subclass of {@link TlcModel}
     */
    T convertRowToModel(final Row row);

    /**
     * <p>
     * Converts collection of {@link Row} objects into the collection of subclass of {@link TlcModel} objects
     * </p>
     *
     * @param rows - Collection of rows that represents the Tlc Models
     * @return Collection of models
     */
    Collection<T> convertRows(final Collection<Row> rows);

    /**
     * <p>
     * Converts stream of {@link Row} objects into the collection of subclass of {@link TlcModel} objects
     * </p>
     *
     * @param rows - Collection of rows that represents the Tlc Models
     * @return Collection of models
     */
    Collection<T> convertRows(final Stream<Row> rows);

    /**
     * <p>
     * Checks whether any rows are having the id of given entity
     * </p>
     *
     * @param model
     * @return If no rows are having the matched entity id, it will return false
     */
    boolean exists(final T model);

    /**
     * <p>
     * Checks whether any rows are having the given id in the organization
     * </p>
     *
     * @param orgId
     * @param id
     * @return If no rows are having the given id, it will return false
     */
    boolean exists(final Long orgId, final Long id);

    /**
     * <p>
     * Fetches the row that matches the given id
     * </p>
     *
     * @param orgId
     * @param id
     * @return If no rows are having the given ids, it will return empty list otherwise it will return the rows
     * that match the one of the given ids
     */
    T get(final Long orgId, final Long id);

    /**
     * <p>
     * Fetches the rows that matches the given ids
     * </p>
     *
     * @param orgId
     * @param ids
     * @return If no rows are having the given ids, it will return empty list otherwise it will return the rows
     * that match the one of the given ids
     */
    Collection<T> get(final Long orgId, final Collection<Long> ids);

    /**
     * <p>
     * Partially fetches values of an entity
     * </p>
     *
     * @param orgId
     * @param id
     * @return Partial values of an entity
     */
    T partialGet(final Long orgId, final Long id);

    /**
     * <p>
     * Fetches the objects available for an organization
     * </p>
     *
     * @param id
     * @return - If no records were created for the organization empty list will be returned
     */
    Collection<T> getByOrgId(final Long id);

    /**
     * @param model
     * @return
     */
    Row fetchRecord(final T model);

    /**
     * <p>
     * Fetches the rows that matches the given query
     * </p>
     *
     * @param orgId
     * @param models
     * @return If no rows are having the given query request, it will return empty container otherwise it will return the rows
     * that match the given ids
     */
    DataContainer fetchRecords(final Long orgId, final Collection<T> models);

    /**
     * <p>
     * Fetches the rows that matches the given query
     * </p>
     *
     * @param orgId
     * @param ids
     * @return If no rows are having the given query request, it will return empty container otherwise it will return the rows
     * that match the given ids
     */
    DataContainer fetchRecordsByIds(final Long orgId, final Collection<Long> ids);

    /**
     * <p>
     * Fetches the rows that matches the given query
     * </p>
     *
     * @param orgId
     * @param selectQuery
     * @return If no rows are matching the given query request, it will return empty container otherwise it will return the rows
     * that in the container
     */
    DataContainer fetchRecords(final Long orgId, SelectQuery selectQuery);


    /**
     * <p>
     * Saves the given record
     * </p>
     *
     * @param model
     * @return Once the record has been saved, it will return the model with an identifier
     */
    T create(final T model);

    /**
     * <p>
     * Saves the given records into the database.
     * Failure of one record doesn't impact other records from create operation
     * </p>
     *
     * @param orgId
     * @param models
     * @return Collection of models with an identifier
     */
    Map<String, Object> create(final Long orgId, final Collection<T> models);

    /**
     * <p>
     * Updates the row that matches the given record
     * </p>
     *
     * @param model
     * @return Updated entity which is a subclass of {@link TlcModel}
     */
    T update(final T model);

    /**
     * <p>
     * Updates the rows that are matches the given records
     * Failure of one record doesn't impact other records from update operation
     * </p>
     *
     * @param orgId
     * @param models
     * @return Collection of updated entities
     */
    Map<String, Object> update(Long orgId, Collection<T> models);

    /**
     * <p>
     * Deletes the row that matches the given record
     * </p>
     *
     * @param model
     */
    void delete(final T model);

    /**
     * <p>
     * Deletes the rows that matches the given records
     * </p>
     *
     * @param orgId
     * @param models
     */
    void delete(final Long orgId, final Collection<T> models);

    /**
     * <p>
     * Deletes the rows in the data container
     * </p>
     *
     * @param dataContainer
     */
    void delete(final DataContainer dataContainer, final Stream<Row> rows);

    /**
     * <p>
     * Deletes the rows in the data container by matching the given criteria of the table
     * </p>
     *
     * @param orgId
     * @param dataContainer
     * @param whereClause
     */
    void delete(final Long orgId, final DataContainer dataContainer, final WhereClause whereClause);

    /**
     * <p>
     * Deletes the rows in the data container by matching the given criteria of the table
     * </p>
     *
     * @param orgId
     * @param dataContainer
     * @param table
     * @param whereClause
     */
    void delete(final Long orgId, final DataContainer dataContainer,
                final Table table, final WhereClause whereClause);

    /**
     * <p>
     * Logs different states of same entity
     * </p>
     *
     * @param model
     * @return
     */
    AuditEntry auditEntry(final T model);
}
