package com.tlc.crm.contact.api.manager;

import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.contact.api.models.LifeCycleStage;
import com.tlc.crm.contact.sql.resource.MCOLIFECYCLESTAGE;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implements the {@link LifeCycleStage} related DB actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 */
public class LifeCycleStageManager extends AbstractConfigManager<LifeCycleStage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifeCycleStageManager.class);

    private static class Instance {
        private static final LifeCycleStageManager INSTANCE = new LifeCycleStageManager();
    }

    private LifeCycleStageManager() {
    }

    public static LifeCycleStageManager getInstance() {
        return LifeCycleStageManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MCOLIFECYCLESTAGE.TABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final LifeCycleStage lifeCycleStage,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Row lifeCycleStageRow = null == existingRow ? new Row(getTable()) : existingRow;

        lifeCycleStageRow.set(MCOLIFECYCLESTAGE.NAME, lifeCycleStage.getName());

        if (lifeCycleStageRow.isNewRow()) {
            lifeCycleStageRow.setOrgId(lifeCycleStage.orgId());
            lifeCycleStage.setId(lifeCycleStageRow.getPKValue());

            dataContainer.addNewRow(lifeCycleStageRow);
        } else {
            dataContainer.updateRow(lifeCycleStageRow);
        }

    }

    /**
     * {@inheritDoc}
     */
    public LifeCycleStage convertRowToModel(final Row row) {
        final LifeCycleStage lifeCycleStage = new LifeCycleStage();

        lifeCycleStage.setId(row.getPKValue());
        lifeCycleStage.setName(row.get(MCOLIFECYCLESTAGE.NAME));
        lifeCycleStage.setOrgId(row.getOrgId());

        return lifeCycleStage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LifeCycleStage partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final LifeCycleStage model) {
        return null;
    }
}
