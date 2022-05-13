package com.tlc.crm.contact.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.contact.api.models.ContactSource;
import com.tlc.crm.contact.sql.resource.MCOCONTACTSOURCE;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implements the {@link ContactSource} related DB actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 */
public class ContactSourceManager extends AbstractConfigManager<ContactSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactSourceManager.class);

    private static class Instance {
        private static final ContactSourceManager INSTANCE = new ContactSourceManager();
    }

    private ContactSourceManager() {
    }

    public static ContactSourceManager getInstance() {
        return ContactSourceManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MCOCONTACTSOURCE.TABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final ContactSource contactSource,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Row contactSourceRow = null == existingRow ? new Row(getTable()) : existingRow;

        contactSourceRow.set(MCOCONTACTSOURCE.VALUE, contactSource.getValue());

        if (contactSourceRow.isNewRow()) {
            contactSourceRow.setOrgId(contactSource.orgId());
            contactSource.setId(contactSourceRow.getPKValue());

            dataContainer.addNewRow(contactSourceRow);
        } else {
            dataContainer.updateRow(contactSourceRow);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ContactSource convertRowToModel(final Row row) {
        final ContactSource contactSource = new ContactSource();

        contactSource.setId(row.getPKValue());
        contactSource.setValue(row.get(MCOCONTACTSOURCE.VALUE));
        contactSource.setOrgId(row.getOrgId());

        return contactSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactSource partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final ContactSource model) {
        return null;
    }

}
