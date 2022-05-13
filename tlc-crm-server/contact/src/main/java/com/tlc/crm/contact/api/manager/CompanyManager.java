package com.tlc.crm.contact.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.contact.api.models.Company;
import com.tlc.crm.contact.sql.resource.MCOCOMPANY;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implements the {@link Company} related DB actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 */
public class CompanyManager extends AbstractConfigManager<Company> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyManager.class);

    private static class Instance {
        private static final CompanyManager INSTANCE = new CompanyManager();
    }

    private CompanyManager() {
    }

    public static CompanyManager getInstance() {
        return CompanyManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MCOCOMPANY.TABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final Company company, final Row existingRow,
                                     final DataContainer dataContainer) {
        final Row companyRow = null == existingRow ? new Row(getTable()) : existingRow;

        companyRow.set(MCOCOMPANY.NAME, company.getName());

        if (companyRow.isNewRow()) {
            companyRow.setOrgId(company.orgId());
            company.setId(companyRow.getPKValue());
            dataContainer.addNewRow(companyRow);
        } else {
            dataContainer.updateRow(companyRow);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company convertRowToModel(Row row) {
        final Company company = new Company();

        company.setId(row.getPKValue());
        company.setName(row.get(MCOCOMPANY.NAME));
        company.setOrgId(row.getOrgId());

        return company;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final Company model) {
        return null;
    }

}
