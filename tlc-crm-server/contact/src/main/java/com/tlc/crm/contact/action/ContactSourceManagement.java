package com.tlc.crm.contact.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.contact.api.manager.ContactSourceManager;
import com.tlc.crm.contact.api.models.ContactSource;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link ContactSource} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/contact/source/mgmt")
public class ContactSourceManagement extends CrmConfigAction<ContactSource> {

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
    public AbstractConfigManager<ContactSource> getConfigManager() {
        return ContactSourceManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactSource convert(final Long orgId, final JsonObject jsonObject) {
        return ContactSourceManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final ContactSource contactSource) {
        return ContactSourceManagement.convertToJson(contactSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContactSource convertPartialData(Long orgId, JsonObject jsonObject) {
        return ContactSourceManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link ContactSource}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link ContactSource}
     */
    public static ContactSource convertToModel(final Long orgId, final JsonObject jsonObject) {
        final ContactSource contactSource = new ContactSource();

        if (jsonObject.containsKey("id")) {
            contactSource.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, contactSource, jsonObject, false);
        return contactSource;
    }

    /**
     * Converts partial data into {@link ContactSource}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static ContactSource convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final ContactSource contactSource = ContactSourceManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, contactSource, jsonObject, true);
        return contactSource;
    }

    /**
     * Sets JSON data into {@link ContactSource}
     *
     * @param orgId
     * @param contactSource
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(final Long orgId, final ContactSource contactSource,
                                           final JsonObject jsonObject, final boolean isPartialData) {
        contactSource.setValue(jsonObject.containsKey("name") ? (String) jsonObject.opt("name")
                : contactSource.getValue());
        contactSource.setOrgId(orgId);
    }

    /**
     * <p>
     * Converts {@link ContactSource} into {@link JsonObject}
     * </p>
     *
     * @param contactSource
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final ContactSource contactSource) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", contactSource.id());
        jsonObject.put("orgId", contactSource.orgId());
        jsonObject.put("name", contactSource.getValue());

        return jsonObject;
    }

}
