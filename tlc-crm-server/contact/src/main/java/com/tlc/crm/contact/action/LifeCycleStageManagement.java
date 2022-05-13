package com.tlc.crm.contact.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmConfigAction;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.contact.api.manager.LifeCycleStageManager;
import com.tlc.crm.contact.api.models.LifeCycleStage;
import com.tlc.web.WebAction;

/**
 * <p>
 * Manages {@link LifeCycleStage} related web actions
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 * @see CrmConfigAction
 */
@WebAction(path = "/contact/stage/mgmt")
public class LifeCycleStageManagement extends CrmConfigAction<LifeCycleStage> {

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
    public AbstractConfigManager<LifeCycleStage> getConfigManager() {
        return LifeCycleStageManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LifeCycleStage convert(final Long orgId, final JsonObject jsonObject) {
        return LifeCycleStageManagement.convertToModel(orgId, jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject convert(final LifeCycleStage lifeCycleStage) {
        return LifeCycleStageManagement.convertToJson(lifeCycleStage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LifeCycleStage convertPartialData(Long orgId, JsonObject jsonObject) {
        return LifeCycleStageManagement.convertPartialDataToModel(orgId, jsonObject);
    }

    /**
     * <p>
     * Converts {@link JsonObject} into {@link LifeCycleStage}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return {@link LifeCycleStage}
     */
    public static LifeCycleStage convertToModel(final Long orgId, final JsonObject jsonObject) {
        final LifeCycleStage lifeCycleStage = new LifeCycleStage();

        if (jsonObject.containsKey("id")) {
            lifeCycleStage.setId(jsonObject.getLong("id"));
        }

        setJsonDataToModel(orgId, lifeCycleStage, jsonObject, false);
        return lifeCycleStage;
    }

    /**
     * Converts partial data into {@link LifeCycleStage}
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public static LifeCycleStage convertPartialDataToModel(Long orgId, JsonObject jsonObject) {
        final LifeCycleStage lifeCycleStage = LifeCycleStageManager.getInstance().get(orgId, jsonObject.getLong("id"));

        setJsonDataToModel(orgId, lifeCycleStage, jsonObject, true);
        return lifeCycleStage;
    }

    /**
     * Sets JSON data into {@link LifeCycleStage}
     *
     * @param orgId
     * @param lifeCycleStage
     * @param jsonObject
     * @param isPartialData
     */
    private static void setJsonDataToModel(final Long orgId, final LifeCycleStage lifeCycleStage,
                                           final JsonObject jsonObject, final boolean isPartialData) {
        lifeCycleStage.setName(jsonObject.containsKey("name") ? (String) jsonObject.opt("name")
                : lifeCycleStage.getName());
        lifeCycleStage.setOrgId(orgId);
    }

    /**
     * <p>
     * Converts {@link LifeCycleStage} into {@link JsonObject}
     * </p>
     *
     * @param lifeCycleStage
     * @return {@link JsonObject}
     */
    public static JsonObject convertToJson(final LifeCycleStage lifeCycleStage) {
        final JsonObject jsonObject = Json.object();

        jsonObject.put("id", lifeCycleStage.id());
        jsonObject.put("name", lifeCycleStage.getName());
        jsonObject.put("orgId", lifeCycleStage.orgId());

        return jsonObject;
    }
}
