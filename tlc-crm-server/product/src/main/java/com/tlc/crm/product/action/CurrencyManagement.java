package com.tlc.crm.product.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.product.api.manager.CurrencyManager;
import com.tlc.crm.product.api.models.Currency;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link Currency} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/product/currency/mgmt")
public class CurrencyManagement extends CrmConfigAction<Currency> {

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
    public AbstractConfigManager<Currency> getConfigManager() {
        return CurrencyManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Currency convert(final Long orgId, final JsonObject jsonObject) {
        return CurrencyManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final Currency currency) {
        return CurrencyManagement.convertToJson(currency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Currency convertPartialData(Long orgId, JsonObject jsonObject) {
        return CurrencyManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link Currency}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link Currency}
     */
    public static Currency convertToModel(final Long orgId, final JsonObject jsonObject) {
        final Currency currency = new Currency();

        if (jsonObject.containsKey("id")) {
            currency.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, currency, jsonObject, false);
        return currency;
    }

    /**
     * Converts partial data into {@link Currency}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static Currency convertPartialDataToModel(final Long orgId, final JsonObject jsonObject) {
        final Currency currency = CurrencyManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, currency, jsonObject, true);

        return currency;
    }

    /**
     * Sets the JSON data to {@link Currency}
     *
     * @param orgId
     * @param currency
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(Long orgId, Currency currency, JsonObject jsonObject, final boolean isPartialData) {
        currency.setName(jsonObject.containsKey("name") ? (String) jsonObject.opt("name") : currency.getName());
        currency.setOrgId(orgId);
    }

    /**
     * <p>
     * Converts {@link Currency} into {@link JsonObject}
     * </p>
     *
     * @param currency
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final Currency currency) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", currency.id());
        jsonObject.put("name", currency.getName());
        jsonObject.put("orgId", currency.orgId());

        return jsonObject;
    }
}
