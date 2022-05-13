package com.tlc.crm.product.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.product.api.models.PriceTag;
import com.tlc.crm.product.sql.resource.MPOPRICETAG;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Criteria;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PriceTagManager extends AbstractConfigManager<PriceTag> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceTagManager.class);

    private static class Instance {
        private static final PriceTagManager INSTANCE = new PriceTagManager();
    }

    private PriceTagManager() {
    }

    public static PriceTagManager getInstance() {
        return PriceTagManager.Instance.INSTANCE;
    }

    @Override
    public Table getTable() {
        return Table.get(MPOPRICETAG.TABLE_NAME);
    }

    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final PriceTag model,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Row row = null == existingRow ? new Row(getTable()) : existingRow;

        row.set(MPOPRICETAG.PRICE, model.getPrice());
        row.set(MPOPRICETAG.CURRENCY_ID, model.getCurrency().id());

        if (null == relatedEntity || null == relatedEntity.id()) {
            row.set(MPOPRICETAG.PRODUCT_ID, model.getProductId());
        } else {
            row.set(MPOPRICETAG.PRODUCT_ID, relatedEntity.id());
            model.setProductId(relatedEntity.id());
        }

        if (row.isNewRow()) {
            row.setOrgId(model.orgId());
            model.setId(row.getPKValue());
            dataContainer.addNewRow(row);
        } else {
            dataContainer.updateRow(row);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriceTag convertRowToModel(final Row row) {
        final PriceTag priceTag = new PriceTag();

        priceTag.setId(row.getPKValue());
        priceTag.setOrgId(row.getOrgId());
        priceTag.setProductId(row.get(MPOPRICETAG.PRODUCT_ID));
        priceTag.setPrice(Double.parseDouble(row.get(MPOPRICETAG.PRICE)));//TODO need to update column type
        priceTag.setCurrency(CurrencyManager.getInstance().get(priceTag.orgId(), (Long) row.get(MPOPRICETAG.CURRENCY_ID)));

        return priceTag;
    }

    /**
     * <p>
     * Fetches the price tags available for the given product
     * </p>
     *
     * @param orgId
     * @param productId
     * @return - If no price tags available, it will return empty list
     */
    public List<PriceTag> getPriceTagsOfProduct(final Long orgId, final Long productId) {
        final Table table = getTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(
                table.getColumn(MPOPRICETAG.PRODUCT_ID), productId));
        final Stream<Row> rows = orgDataStore(orgId).get(table, whereClause).getRows(table);
        final List<PriceTag> priceTags = new ArrayList<>();

        priceTags.addAll(convertRows(rows));
        return priceTags;
    }

    /**
     * <p>
     * Deletes the price tags that are not associated with the product
     * </p>
     *
     * @param orgId
     * @param contactId
     * @param priceTags
     * @param dataContainer
     */
    public void deleteUnmappedPriceTags(final Long orgId, final Long contactId, final Collection<PriceTag> priceTags,
                                        final DataContainer dataContainer) {
        final Table table = getTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(table.getColumn(MPOPRICETAG.PRODUCT_ID),
                contactId)).and(Criteria.notIn(table.getPKColumn(),
                priceTags.stream().map(PriceTag::id).collect(Collectors.toUnmodifiableSet())));

        delete(orgId, dataContainer, whereClause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriceTag partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final PriceTag model) {
        return null;
    }

}
