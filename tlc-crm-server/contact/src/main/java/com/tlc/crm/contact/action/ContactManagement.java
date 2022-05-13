package com.tlc.crm.contact.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.contact.api.manager.ContactManager;
import com.tlc.crm.contact.api.models.Company;
import com.tlc.crm.contact.api.models.Contact;
import com.tlc.crm.contact.api.models.ContactEmail;
import com.tlc.crm.contact.api.models.ContactMobile;
import com.tlc.crm.contact.api.models.ContactSource;
import com.tlc.crm.contact.api.models.LifeCycleStage;
import com.tlc.web.WebAction;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Abishek
 * @version 1.0
 */
@WebAction(path = "/contact/mgmt")
public class ContactManagement extends CrmConfigAction<Contact> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(CrmRequest request, CrmResponse response) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractConfigManager<Contact> getConfigManager() {
        return ContactManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact convert(final Long orgId, final JsonObject jsonObject) {
        return ContactManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final Contact contact) {
        return ContactManagement.convertToJson(contact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact convertPartialData(Long orgId, JsonObject jsonObject) {
        return ContactManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link Contact}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link Contact}
     */
    public static Contact convertToModel(final Long orgId, final JsonObject jsonObject) {
        final Contact contact = new Contact();

        if (jsonObject.containsKey("id")) {
            contact.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, contact, jsonObject, false);
        return contact;
    }

    /**
     * Converts partial data into {@link Contact}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static Contact convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final Contact contact = ContactManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, contact, jsonObject, true);
        return contact;
    }

    /**
     * Sets JSON data into {@link Contact}
     *
     * @param orgId
     * @param contact
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(final Long orgId, final Contact contact, final JsonObject jsonObject,
                                           final boolean isPartialData) {
        contact.setOrgId(orgId);
        contact.setOwnerId(1l);
        contact.setFirstName(jsonObject.containsKey("firstName") ? (String) jsonObject.opt("firstName")
                : contact.getFirstName());
        contact.setLastName(jsonObject.containsKey("lastName") ? (String) jsonObject.opt("lastName")
                : contact.getLastName());
        setEmailDetails(orgId, contact, jsonObject, isPartialData);
        setMobileDetails(orgId, contact, jsonObject, isPartialData);

        if (jsonObject.containsKey("company")) {
            final JsonObject companyJson = jsonObject.getJsonObject("company");
            final Company company = isPartialData
                    ? CompanyManagement.convertPartialDataToModel(orgId, companyJson)
                    : CompanyManagement.convertToModel(orgId, companyJson);

            contact.setCompany(company);
        }

        if (jsonObject.containsKey("contactSource")) {
            final JsonObject contactSourceJson = jsonObject.getJsonObject("contactSource");
            final ContactSource contactSource = isPartialData
                    ? ContactSourceManagement.convertPartialDataToModel(orgId, contactSourceJson)
                    : ContactSourceManagement.convertToModel(orgId, contactSourceJson);

            contact.setContactSource(contactSource);
        }

        if (jsonObject.containsKey("lifeCycleStage")) {
            final JsonObject lifeCycleStageJson = jsonObject.getJsonObject("lifeCycleStage");
            final LifeCycleStage lifeCycleStage = isPartialData
                    ? LifeCycleStageManagement.convertPartialDataToModel(orgId, lifeCycleStageJson)
                    : LifeCycleStageManagement.convertToModel(orgId, lifeCycleStageJson);

            contact.setLifeCycleStage(lifeCycleStage);
        }
    }

    /**
     * Sets {@link ContactMobile} details into {@link Contact}
     *
     * @param orgId
     * @param contact
     * @param jsonObject
     * @param isPartialData
     */
    private static void setMobileDetails(Long orgId, Contact contact, JsonObject jsonObject, boolean isPartialData) {
        final JsonArray mobiles = jsonObject.optJsonArray("mobiles");
        final List<ContactMobile> oldMobiles = contact.getMobiles();

        contact.setMobiles(null);

        if (jsonObject.containsKey("mobiles")) {
            for (int i = 0; i < mobiles.size(); i++) {
                final JsonObject mobile = mobiles.optJsonObject(i);
                final ContactMobile contactMobile = new ContactMobile();

                if (mobile.containsKey("id")) {
                    contactMobile.setId(mobile.getLong("id"));
                }

                contactMobile.setContactId(contact.id());
                contactMobile.setType(mobile.optString("type", null));
                contactMobile.setNumber(mobile.optString("value", null));
                contactMobile.setOrgId(orgId);
                contact.addMobile(contactMobile);
            }

            if (isPartialData) {
                final Set<Long> currentMobileIds = contact.getMobiles().stream()
                        .filter(contactMobile -> null != contactMobile.id())
                        .map(ContactMobile::id).collect(Collectors.toSet());

                oldMobiles.stream().filter(oldMobile -> !currentMobileIds.contains(oldMobile.id()))
                        .forEach(oldMobile -> contact.addMobile(oldMobile));
            }
        } else if (isPartialData) {
            contact.setMobiles(oldMobiles);
        }
    }

    /**
     * Sets {@link ContactEmail} details into {@link Contact}
     *
     * @param orgId
     * @param contact
     * @param jsonObject
     * @param isPartialData
     */
    private static void setEmailDetails(Long orgId, Contact contact, JsonObject jsonObject, boolean isPartialData) {
        final JsonArray emails = jsonObject.optJsonArray("emails");
        final List<ContactEmail> oldEmails = contact.getEmails();

        contact.setEmails(null);

        if (jsonObject.containsKey("emails")) {
            for (int i = 0; i < emails.size(); i++) {
                final JsonObject email = emails.optJsonObject(i);
                final ContactEmail contactEmail = new ContactEmail();

                if (email.containsKey("id")) {
                    contactEmail.setId(email.getLong("id"));
                }

                contactEmail.setContactId(contact.id());
                contactEmail.setOrgId(orgId);
                contactEmail.setType(email.optString("type", null));
                contactEmail.setValue(email.optString("value", null));
                contact.addEmail(contactEmail);
            }

            if (isPartialData) {
                final Set<Long> currentEmailIds = contact.getEmails().stream()
                        .filter(contactEmail -> null != contactEmail.id())
                        .map(ContactEmail::id).collect(Collectors.toSet());

                oldEmails.stream().filter(oldEmail -> !currentEmailIds.contains(oldEmail.id()))
                        .forEach(oldEmail -> contact.addEmail(oldEmail));
            }
        } else if (isPartialData) {
            contact.setEmails(oldEmails);
        }
    }

    /**
     * <p>
     * Converts {@link Contact} into {@link JsonObject}
     * </p>
     *
     * @param contact
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final Contact contact) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", contact.id());
        jsonObject.put("orgId", contact.orgId());
        jsonObject.put("firstName", contact.getFirstName());
        jsonObject.put("lastName", contact.getLastName());

        final JsonArray emails = Json.array();

        contact.getEmails().forEach(email -> {
            final JsonObject emailObj = Json.object();

            emailObj.put("id", email.id());
            emailObj.put("type", email.getType());
            emailObj.put("value", email.getValue());

            emails.put(emailObj);
        });

        final JsonArray mobiles = Json.array();

        contact.getMobiles().forEach(mobile -> {
            final JsonObject mobileObj = Json.object();

            mobileObj.put("id", mobile.id());
            mobileObj.put("type", mobile.getType());
            mobileObj.put("value", mobile.getNumber());

            mobiles.put(mobileObj);
        });

        jsonObject.put("emails", emails);
        jsonObject.put("mobiles", mobiles);

        jsonObject.put("company", CompanyManagement.convertToJson(contact.getCompany()));
        jsonObject.put("lifeCycleStage", LifeCycleStageManagement.convertToJson(contact.getLifeCycleStage()));
        jsonObject.put("contactSource", ContactSourceManagement.convertToJson(contact.getContactSource()));

        return jsonObject;
    }
}