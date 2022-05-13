package com.tlc.crm.product.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.product.api.manager.PriceTagManager;
import com.tlc.crm.product.api.models.Currency;
import com.tlc.crm.product.api.models.PriceTag;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link PriceTag} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/product/pricetag/mgmt")
public class PriceTagManagement extends CrmConfigAction<PriceTag> {

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
    public AbstractConfigManager<PriceTag> getConfigManager() {
        return PriceTagManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriceTag convert(final Long orgId, final JsonObject jsonObject) {
        return PriceTagManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final PriceTag priceTag) {
        return PriceTagManagement.convertToJson(priceTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PriceTag convertPartialData(Long orgId, JsonObject jsonObject) {
        return PriceTagManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link PriceTag}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link PriceTag}
     */
    public static PriceTag convertToModel(final Long orgId, final JsonObject jsonObject) {
        final PriceTag priceTag = new PriceTag();

        if (jsonObject.containsKey("id")) {
            priceTag.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, priceTag, jsonObject, false);
        return priceTag;
    }

    /**
     * Converts partial data to model
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static PriceTag convertPartialDataToModel(final Long orgId, final JsonObject jsonObject) {
        final PriceTag priceTag = PriceTagManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, priceTag, jsonObject, true);

        return priceTag;
    }

    /**
     * Sets the json data into {@link PriceTag}
     *
     * @param orgId
     * @param priceTag
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(Long orgId, PriceTag priceTag, JsonObject jsonObject, boolean isPartialData) {
        final Object price = jsonObject.opt("price");

        priceTag.setOrgId(orgId);
        priceTag.setPrice(jsonObject.containsKey("price") ? null == price ? 0 : (long) price : priceTag.getPrice());
        priceTag.setProductId(jsonObject.containsKey("productId") ? (Long) jsonObject.opt("productId")
                : priceTag.getProductId());

        if (jsonObject.containsKey("currency")) {
            final Currency currency = isPartialData
                    ? CurrencyManagement.convertPartialDataToModel(orgId, jsonObject.getJsonObject("currency"))
                    : CurrencyManagement.convertToModel(orgId, jsonObject.getJsonObject("currency"));

            priceTag.setCurrency(currency);
        }

    }

    /**
     * <p>
     * Converts {@link PriceTag} into {@link JsonObject}
     * </p>
     *
     * @param priceTag
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final PriceTag priceTag) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", priceTag.id());
        jsonObject.put("price", priceTag.getPrice());
        jsonObject.put("orgId", priceTag.orgId());
        jsonObject.put("currency", CurrencyManagement.convertToJson(priceTag.getCurrency()));
        jsonObject.put("productId", priceTag.getProductId());

        return jsonObject;
    }
}
