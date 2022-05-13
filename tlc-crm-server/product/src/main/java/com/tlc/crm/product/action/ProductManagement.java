package com.tlc.crm.product.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.product.api.manager.ProductManager;
import com.tlc.crm.product.api.models.Category;
import com.tlc.crm.product.api.models.PriceTag;
import com.tlc.crm.product.api.models.Product;
import com.tlc.web.WebAction;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Manages {@link Product} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/product/mgmt")
public class ProductManagement extends CrmConfigAction<Product> {

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
    public AbstractConfigManager<Product> getConfigManager() {
        return ProductManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product convert(final Long orgId, final JsonObject jsonObject) {
        return ProductManagement.convertJsonToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final Product product) {
        return ProductManagement.convertModelToJson(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product convertPartialData(Long orgId, JsonObject jsonObject) {
        return ProductManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link Product}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link JsonObject}
     */
    public static Product convertJsonToModel(final Long orgId, final JsonObject jsonObject) {
        final Product product = new Product();

        if (jsonObject.containsKey("id")) {
            product.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, product, jsonObject, false);
        return product;
    }

    /**
     * Converts partial data into {@link Product}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static Product convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final Product product = ProductManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, product, jsonObject, true);
        return product;
    }

    /**
     * Sets json data into {@link Product}
     *
     * @param orgId
     * @param product
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(Long orgId, Product product, JsonObject jsonObject, boolean isPartialData) {
        final List<PriceTag> oldPriceTags = product.getPriceTags();

        product.setOrgId(orgId);
        product.setName(jsonObject.containsKey("name") ? (String) jsonObject.opt("name") : product.getName());
        product.setDescription(jsonObject.containsKey("description") ? (String) jsonObject.opt("description")
                : product.getDescription());
        product.setSkuNumber(jsonObject.containsKey("skuNumber") ? (String) jsonObject.opt("skuNumber")
                : product.getSkuNumber());
        product.setProductCode(jsonObject.containsKey("productCode") ? (String) jsonObject.opt("productCode")
                : product.getProductCode());

        product.setPriceTags(null);

        if (jsonObject.containsKey("priceTags")) {
            final JsonArray tagArray = jsonObject.getJsonArray("priceTags");

            for (int i = 0; i < tagArray.size(); i++) {
                final JsonObject priceTagJson = tagArray.getJsonObject(i);
                final PriceTag priceTag = isPartialData
                        ? PriceTagManagement.convertPartialDataToModel(orgId, priceTagJson)
                        : PriceTagManagement.convertToModel(orgId, priceTagJson);

                priceTag.setProductId(product.id());
                product.addPriceTag(priceTag);
            }

            if (isPartialData) {
                final Set<Long> currentPriceTagIds = product.getPriceTags().stream()
                        .filter(priceTag -> null != priceTag.id())
                        .map(PriceTag::id).collect(Collectors.toSet());

                oldPriceTags.stream().filter(oldPriceTag -> !currentPriceTagIds.contains(oldPriceTag.id()))
                        .forEach(oldPriceTag -> product.addPriceTag(oldPriceTag));
            }
        } else if (isPartialData) {
            product.setPriceTags(oldPriceTags);
        }

        if (jsonObject.containsKey("category")) {
            final JsonObject categoryJson = jsonObject.getJsonObject("category");
            final Category category = isPartialData
                    ? CategoryManagement.convertPartialDataToModel(orgId, categoryJson)
                    : CategoryManagement.convertToModel(orgId, categoryJson);

            product.setCategory(category);
        }

    }

    /**
     * <p>
     * Converts {@link Product} into {@link JsonObject}
     * </p>
     *
     * @param product
     * @return {@link JsonObject}
     */
    public static JsonObject convertModelToJson(final Product product) {
        final JsonObject jsonObject = Json.object();
        final List<PriceTag> priceTags = product.getPriceTags();
        final JsonArray tagsArray = Json.array();

        jsonObject.put("id", product.id());
        jsonObject.put("name", product.getName());
        jsonObject.put("orgId", product.orgId());
        jsonObject.put("description", product.getDescription());
        jsonObject.put("skuNumber", product.getSkuNumber());
        jsonObject.put("productCode", product.getProductCode());
        jsonObject.put("priceTags", tagsArray);
        jsonObject.put("category", CategoryManagement.convertToJson(product.getCategory()));

        if (null != priceTags && !priceTags.isEmpty()) {
            priceTags.stream().forEach(priceTag -> {
                tagsArray.put(PriceTagManagement.convertToJson(priceTag));
            });
        }

        return jsonObject;
    }
}
