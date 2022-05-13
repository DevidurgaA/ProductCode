package com.tlc.crm.contact.api.manager;

import com.tlc.commons.code.ErrorCode;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.config.AuditEntry;
import com.tlc.crm.contact.api.models.Company;
import com.tlc.crm.contact.api.models.Contact;
import com.tlc.crm.contact.api.models.ContactEmail;
import com.tlc.crm.contact.api.models.ContactMobile;
import com.tlc.crm.contact.internal.status.ContactErrorCodes;
import com.tlc.crm.contact.sql.resource.MCOCONTACT;
import com.tlc.crm.contact.sql.resource.MCOCONTACTEMAIL;
import com.tlc.crm.contact.sql.resource.MCOCONTACTMOBILE;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Criteria;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.validator.TlcModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Implements the {@link Contact} related DB actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 */
public class ContactManager extends AbstractConfigManager<Contact> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactManager.class);

    private static class Instance {
        private static final ContactManager INSTANCE = new ContactManager();
    }

    private ContactManager() {
    }

    public static ContactManager getInstance() {
        return ContactManager.Instance.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table getTable() {
        return Table.get(MCOCONTACT.TABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRowIntoContainer(final TlcModel relatedEntity, final Contact contact,
                                     final Row existingRow, final DataContainer dataContainer) {
        final Company company = contact.getCompany();
        final Row row = null == existingRow ? new Row(getTable()) : existingRow;

        //TODO update company, stage, source on contact update call. Need to get confirmation
        if (null == contact.getCompanyId()) {
            CompanyManager.getInstance().loadRowIntoContainer(company, dataContainer);
        }

        if (null == contact.getLifeCycleStage().id()) {
            LifeCycleStageManager.getInstance().loadRowIntoContainer(contact.getLifeCycleStage(), dataContainer);
        }

        if (null == contact.getContactSource().id()) {
            ContactSourceManager.getInstance().loadRowIntoContainer(contact.getContactSource(), dataContainer);
        }

        row.set(MCOCONTACT.FIRST_NAME, contact.getFirstName());
        row.set(MCOCONTACT.LAST_NAME, contact.getLastName());
        row.set(MCOCONTACT.OWNER_ID, contact.getOwnerId());
        row.set(MCOCONTACT.COMPANY_ID, contact.getCompany().id());
        row.set(MCOCONTACT.LIFE_CYCLE_STAGE_ID, contact.getLifeCycleStage().id());
        row.set(MCOCONTACT.CONTACT_SOURCE_ID, contact.getContactSource().id());

        if (row.isNewRow()) {
            row.setOrgId(contact.orgId());
            contact.setId(row.getPKValue());
            dataContainer.addNewRow(row);
        } else {
            dataContainer.updateRow(row);
        }

        contact.getEmails().stream().forEach(email -> loadEmailIntoContainer(contact, email, dataContainer));
        contact.getMobiles().stream().forEach(mobile -> loadMobileRowIntoContainer(contact, mobile, dataContainer));

        if (!row.isNewRow()) {
            deleteUnmappedEmails(contact.orgId(), contact.id(), contact.getEmails(), dataContainer);
            deleteUnmappedMobileNumbers(contact.orgId(), contact.id(), contact.getMobiles(), dataContainer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Contact convertRowToModel(final Row row) {
        final Contact contact = new Contact();

        contact.setId(row.getPKValue());
        contact.setFirstName(row.get(MCOCONTACT.FIRST_NAME));
        contact.setLastName(row.get(MCOCONTACT.LAST_NAME));
        contact.setOrgId(row.getOrgId());
        contact.setEmails(getEmails(contact.orgId(), contact.id()));
        contact.setMobiles(getMobileNumbers(contact.orgId(), contact.id()));
        contact.setCompany(CompanyManager.getInstance().get(row.getOrgId(), (Long) row.get(MCOCONTACT.COMPANY_ID)));
        contact.setContactSource(ContactSourceManager.getInstance().get(row.getOrgId(),
                (Long) row.get(MCOCONTACT.CONTACT_SOURCE_ID)));
        contact.setLifeCycleStage(LifeCycleStageManager.getInstance().get(row.getOrgId(),
                (Long) row.get(MCOCONTACT.LIFE_CYCLE_STAGE_ID)));

        return contact;
    }

    /**
     * Fetches the email table definitions
     */
    public Table getEmailTable() {
        return Table.get(MCOCONTACTEMAIL.TABLE);
    }

    /**
     * Loads the email into container
     *
     * @param relatedEntity
     * @param email
     * @param dataContainer
     */
    public void loadEmailIntoContainer(final TlcModel relatedEntity, final ContactEmail email,
                                       final DataContainer dataContainer) {
        final Table emailTable = getEmailTable();
        final Row emailRow = null == email.id() ? new Row(emailTable)
                : orgDataStore(email.orgId()).get(emailTable, email.id());

        if (null == emailRow) {
            throw ErrorCode.get(ContactErrorCodes.CONTACT_EMAIL_NOT_FOUND, "ID : " + email.id());
        }

        emailRow.set(MCOCONTACTEMAIL.CONTACT_ID,
                null == relatedEntity || null == relatedEntity.id() ? email.getContactId() : relatedEntity.id());
        emailRow.set(MCOCONTACTEMAIL.VALUE, email.getValue());
        emailRow.set(MCOCONTACTEMAIL.TYPE, email.getType());

        if (emailRow.isNewRow()) {
            emailRow.setOrgId(email.orgId());
            email.setId(emailRow.getPKValue());
            dataContainer.addNewRow(emailRow);
        } else {
            dataContainer.updateRow(emailRow);
        }
    }

    /**
     * Converts the row into {@link ContactEmail}
     *
     * @param row
     * @return
     */
    private ContactEmail convertRowToEmail(final Row row) {
        final ContactEmail email = new ContactEmail();

        email.setId(row.getPKValue());
        email.setContactId(row.get(MCOCONTACTEMAIL.CONTACT_ID));
        email.setValue(row.get(MCOCONTACTEMAIL.VALUE));
        email.setType(row.get(MCOCONTACTEMAIL.TYPE));
        email.setOrgId(row.getOrgId());

        return email;
    }

    /**
     * <p>
     * Fetches the email details of the contact
     * </p>
     *
     * @param orgId
     * @param contactId
     */
    private List<ContactEmail> getEmails(final Long orgId, final Long contactId) {
        final Table emailTable = getEmailTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(
                emailTable.getColumn(MCOCONTACTEMAIL.CONTACT_ID), contactId));
        final Stream<Row> rows = orgDataStore(orgId).get(emailTable, whereClause).getRows(emailTable);


        return rows.map(this::convertRowToEmail).collect(Collectors.toUnmodifiableList());
    }

    /**
     * <p>
     * Deletes the unmapped email details of the contact in data container
     * </p>
     *
     * @param orgId
     * @param contactId
     */
    private void deleteUnmappedEmails(final Long orgId, final Long contactId, final Collection<ContactEmail> contactEmails,
                                      final DataContainer dataContainer) {
        final Table emailTable = getEmailTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(emailTable.getColumn(MCOCONTACTEMAIL.CONTACT_ID), contactId))
                .and(new WhereClause(Criteria.notIn(emailTable.getPKColumn(),
                        contactEmails.stream().map(ContactEmail::id).collect(Collectors.toUnmodifiableSet()))));

        delete(orgId, dataContainer, emailTable, whereClause);
    }

    /**
     * <p>
     * Fetches the mobile table definition
     * </p>
     *
     * @return
     */
    private Table getMobileTable() {
        return Table.get(MCOCONTACTMOBILE.TABLE);
    }

    /**
     * <p>
     * Loads mobile row into container
     * </p>
     *
     * @return
     */
    private void loadMobileRowIntoContainer(final TlcModel relatedEntity, final ContactMobile mobile,
                                            final DataContainer dataContainer) {
        final Table table = getMobileTable();
        final Row mobileRow = null == mobile.id() ? new Row(table) :
                orgDataStore(mobile.orgId()).get(table, mobile.id());

        if (null == mobileRow) {
            throw ErrorCode.get(ContactErrorCodes.CONTACT_MOBILE_NOT_FOUND, "ID : " + mobile.id());
        }

        mobileRow.set(MCOCONTACTMOBILE.CONTACT_ID,
                null == relatedEntity || null == relatedEntity.id() ? mobile.getContactId() : relatedEntity.id());
        mobileRow.set(MCOCONTACTMOBILE.NUMBER, mobile.getNumber());
        mobileRow.set(MCOCONTACTMOBILE.TYPE, mobile.getType());

        if (mobileRow.isNewRow()) {
            mobileRow.setOrgId(mobile.orgId());
            mobile.setId(mobileRow.getPKValue());
            dataContainer.addNewRow(mobileRow);
        } else {
            dataContainer.updateRow(mobileRow);
        }

    }

    /**
     * <p>
     * Fetches the mobile number details of the contact
     * </p>
     *
     * @param orgId
     * @param contactId
     * @return
     */
    private List<ContactMobile> getMobileNumbers(final Long orgId, final Long contactId) {
        final Table mobileTable = getMobileTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(mobileTable.getColumn(MCOCONTACTMOBILE.CONTACT_ID),
                contactId));
        final Stream<Row> rows = orgDataStore(orgId).get(mobileTable, whereClause).getRows(mobileTable);

        return rows.map(this::convertRowToMobile).collect(Collectors.toUnmodifiableList());
    }

    /**
     * <p>
     * Deletes unmapped mobile numbers of the contact in container
     * </p>
     *
     * @param orgId
     * @param contactId
     * @param mobiles
     * @param dataContainer
     */
    private void deleteUnmappedMobileNumbers(final Long orgId, final Long contactId,
                                             final Collection<ContactMobile> mobiles, final DataContainer dataContainer) {
        final Table mobileTable = getMobileTable();
        final WhereClause whereClause = new WhereClause(Criteria.eq(mobileTable.getColumn(MCOCONTACTMOBILE.CONTACT_ID),
                contactId)).and(Criteria.notIn(mobileTable.getPKColumn(),
                mobiles.stream().map(ContactMobile::id).collect(Collectors.toUnmodifiableSet())));

        delete(orgId, dataContainer, mobileTable, whereClause);
    }

    /**
     * {@inheritDoc}
     */
    private ContactMobile convertRowToMobile(final Row row) {
        final ContactMobile mobile = new ContactMobile();

        mobile.setId(row.getPKValue());
        mobile.setContactId(row.get(MCOCONTACTMOBILE.CONTACT_ID));
        mobile.setNumber(row.get(MCOCONTACTMOBILE.NUMBER));
        mobile.setType(row.get(MCOCONTACTMOBILE.TYPE));
        mobile.setOrgId(row.getOrgId());

        return mobile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact partialGet(final Long orgId, final Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuditEntry auditEntry(final Contact model) {
        return null;
    }
}
