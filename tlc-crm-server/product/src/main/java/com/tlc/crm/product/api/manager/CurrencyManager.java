package com.tlc.crm.product.api.manager;

import com.tlc.commons.code.ErrorCode;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.product.api.models.Currency;
import com.tlc.crm.product.internal.status.ProductErrorCodes;
import com.tlc.crm.product.sql.resource.MPOCURRENCY;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyManager extends AbstractConfigManager<Currency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManager.class);

    private static class Instance {
        private static final CurrencyManager INSTANCE = new CurrencyManager();
    }

    private CurrencyManager() {
    }

    public static CurrencyManager getInstance() {
        return CurrencyManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MPOCURRENCY.TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final Currency model,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Row row = null == existingRow ? new Row(getTable()) : existingRow;

        row.set(MPOCURRENCY.NAME, model.getName());

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
    public Currency convertRowToModel(final Row row) {
        final Currency currency = new Currency();

        currency.setId(row.getPKValue());
        currency.setOrgId(row.getOrgId());
        currency.setName(row.get(MPOCURRENCY.NAME));

        return currency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Currency partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(Currency model) {
        return null;
    }
}
