package com.tlc.crm.product.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.product.api.models.Category;
import com.tlc.crm.product.sql.resource.MPOCATEGGORY;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryManager extends AbstractConfigManager<Category> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryManager.class);

    private static class Instance {
        private static final CategoryManager INSTANCE = new CategoryManager();
    }

    private CategoryManager() {
    }

    public static CategoryManager getInstance() {
        return CategoryManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MPOCATEGGORY.TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final Category model,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Row row = null == existingRow ? new Row(getTable()) : existingRow;

        row.set(MPOCATEGGORY.NAME, model.getName());

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
    public Category convertRowToModel(final Row row) {
        final Category category = new Category();

        category.setId(row.getPKValue());
        category.setOrgId(row.getOrgId());
        category.setName(row.get(MPOCATEGGORY.NAME));

        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final Category model) {
        return null;
    }

}
