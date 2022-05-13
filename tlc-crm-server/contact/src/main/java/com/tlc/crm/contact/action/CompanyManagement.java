package com.tlc.crm.contact.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.contact.api.manager.CompanyManager;
import com.tlc.crm.contact.api.models.Company;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link Company} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/contact/company/mgmt")
public class CompanyManagement extends CrmConfigAction<Company> {

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
    public AbstractConfigManager<Company> getConfigManager() {
        return CompanyManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    public Company convert(final Long orgId, final JsonObject jsonObject) {
        return CompanyManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    public JsonObject convert(final Company company) {
        return CompanyManagement.convertToJson(company);
    }

    @Override
    public Company convertPartialData(Long orgId, JsonObject jsonObject) {
        return CompanyManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link Company}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link Company}
     */
    public static Company convertToModel(final Long orgId, final JsonObject jsonObject) {
        final Company company = new Company();

        if (jsonObject.containsKey("id")) {
            company.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, company, jsonObject, false);
        return company;
    }

    /**
     * Converts partial data into {@link Company}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static Company convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final Company company = CompanyManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, company, jsonObject, true);
        return company;
    }

    /**
     * Sets JSON data into {@link Company}
     *
     * @param orgId
     * @param company
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(final Long orgId, final Company company, final JsonObject jsonObject,
                                           final boolean isPartialData) {
        company.setName(jsonObject.containsKey("name") ? (String) jsonObject.opt("name") : company.getName());
        company.setOrgId(orgId);
    }

    /**
     * <p>
     * Converts {@link Company} into {@link JsonObject}
     * </p>
     *
     * @param company
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final Company company) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", company.id());
        jsonObject.put("name", company.getName());
        jsonObject.put("orgId", company.orgId());

        return jsonObject;
    }
}
