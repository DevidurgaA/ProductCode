package com.tlc.crm.product.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.product.api.manager.CategoryManager;
import com.tlc.crm.product.api.models.Category;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link Category} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/product/category/mgmt")
public class CategoryManagement extends CrmConfigAction<Category> {

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
    public AbstractConfigManager<Category> getConfigManager() {
        return CategoryManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category convert(final Long orgId, final JsonObject jsonObject) {
        return CategoryManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final Category category) {
        return CategoryManagement.convertToJson(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category convertPartialData(Long orgId, JsonObject jsonObject) {
        return CategoryManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link Category}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link JsonObject}
     */
    public static Category convertToModel(final Long orgId, final JsonObject jsonObject) {
        final Category category = new Category();

        setJsonDataToModel(orgId, category, jsonObject, false);

        if (jsonObject.containsKey("id")) {
            category.setId(jsonObject.getLong("id"));
        }

        return category;
    }

    /**
     * Converts partial data into {@link Category}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static Category convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final Category category = CategoryManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, category, jsonObject, true);

        return category;
    }

    /**
     * Sets the JSON data to model
     *
     * @param orgId
     * @param category
     * @param jsonObject
     */
    private static void setJsonDataToModel(final Long orgId, Category category, JsonObject jsonObject, final boolean isPartialData) {
        category.setName(jsonObject.containsKey("name") ? (String) jsonObject.opt("name") : category.getName());
        category.setOrgId(orgId);
    }

    /**
     * <p>
     * Converts {@link Category} into {@link JsonObject}
     * </p>
     *
     * @param category
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final Category category) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", category.id());
        jsonObject.put("name", category.getName());
        jsonObject.put("orgId", category.orgId());

        return jsonObject;
    }
}
