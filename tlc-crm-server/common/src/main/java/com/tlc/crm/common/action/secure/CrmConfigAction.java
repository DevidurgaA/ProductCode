package com.tlc.crm.common.action.secure;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.crm.common.internal.resource.CommonErrorCodes;
import com.tlc.validator.ModelValidator;
import com.tlc.validator.TlcModel;
import com.tlc.validator.ValidatorAccess;
import com.tlc.validator.type.Group.Create;
import com.tlc.validator.type.Group.Delete;
import com.tlc.validator.type.Group.Update;
import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmConfigAction<T extends TlcModel> extends CrmSecureAction {

    @Override
    public final void sProcess(CrmRequest request, CrmResponse crmResponse) {
        final JsonObject responseJson = Json.object();
        final JsonObject requestJson = request.getRequestJson();
        final String type = requestJson.getString("type");
        final JsonObject data = requestJson.getJsonObject("data");
        final Long orgId = request.orgId();

        switch (type) {
            case "get" -> {
                crmResponse.put("data", convert(get(orgId, data.getLong("id"))));
            }
            case "create" -> {
                create(crmResponse, orgId, List.of(convert(orgId, data)));
            }
            case "bCreate" -> {
                create(crmResponse, orgId, convert(orgId, data.getJsonArray("list")));
            }
            case "update" -> {
                update(crmResponse, convert(orgId, data));
            }
            case "bUpdate" -> {
                update(crmResponse, orgId, convert(orgId, data.getJsonArray("list")));
            }
            case "pUpdate" -> {
                update(crmResponse, convertPartialData(orgId, data));
            }
            case "delete" -> {
                delete(crmResponse, get(orgId, data.getLong("id")));
            }
            case "bDelete" -> {
                final Set<Long> ids = data.getJsonArray("ids").toList().stream()
                        .map(id -> (Long) id).collect(Collectors.toUnmodifiableSet());

                delete(crmResponse, orgId, get(orgId, ids));
            }
            case "list" -> {
                final Collection<T> models = getConfigManager().getByOrgId(data.getLong("orgId"));

                crmResponse.put("data", convert(models));
            }
            case "exists" -> {
                responseJson.put("found", exists(convert(orgId, data)));
            }
            default -> {
                handle(request, crmResponse);
            }
        }

    }

    /**
     * <p>
     * Handles non default config actions
     * </p>
     *
     * @param request
     * @param response
     */
    public abstract void handle(CrmRequest request, CrmResponse response);

    /**
     * <p>
     * Creates an entity
     * </p>
     *
     * @param model
     * @return
     */
    public void create(final CrmResponse crmResponse, final T model) {
        create(crmResponse, model.orgId(), List.of(model));
    }

    /**
     * <p>
     * Creates entities
     * </p>
     *
     * @param models
     * @return
     */
    public void create(final CrmResponse crmResponse, final Long orgId, final Collection<T> models) {
        final Map<String, Object> response = new HashMap<>();
        final Collection<String> errors = validate(models, Create.class);

        response.put("validationErrors", new ArrayList<>(errors));

        if (errors.isEmpty()) {
            final Map<String, Object> bCreateResponse = getConfigManager().create(orgId, models);

            response.putAll(bCreateResponse);
        }

        final Collection<?> failedRecords = response.containsKey("failedRecordsIndex")
                ? (Collection<?>) response.get("failedRecordsIndex") : Collections.emptyList();
        final Collection<?> createdRecords = response.containsKey("createdRecords")
                ? (Collection<?>) response.get("createdRecords") : Collections.emptyList();

        crmResponse.put("validationErrors", crmResponse.resolveI18nString(errors));
        crmResponse.put("createdRecords", createdRecords);
        crmResponse.put("failedRecordsIndex", failedRecords);

        if (createdRecords.isEmpty() || !errors.isEmpty() || !failedRecords.isEmpty()) {
            crmResponse.setErrorCode(ErrorCode.get(CommonErrorCodes.CREATE_ACTION_FAILED));
        }
    }

    /**
     * <p>
     * Updates the given entity
     * </p>
     *
     * @param model
     * @return
     */
    public void update(final CrmResponse crmResponse, final T model) {
        update(crmResponse, model.orgId(), List.of(model));
    }

    /**
     * <p>
     * Updates the given entities
     * </p>
     *
     * @param models
     */
    public void update(final CrmResponse crmResponse, final Long orgId, final Collection<T> models) {
        final Map<String, Object> response = new HashMap<>();
        final Collection<String> errors = validate(models, Update.class);

        if (errors.isEmpty()) {
            final Map<String, Object> bUpdateResponse = getConfigManager().update(orgId, models);

            response.putAll(bUpdateResponse);
        }

        final Collection<?> failedRecords = response.containsKey("failedRecords")
                ? (Collection<?>) response.get("failedRecords") : Collections.emptyList();
        final Collection<?> updatedRecords = response.containsKey("updatedRecords")
                ? (Collection<?>) response.get("updatedRecords") : Collections.emptyList();

        crmResponse.put("validationErrors", crmResponse.resolveI18nString(errors));
        crmResponse.put("updatedRecords", updatedRecords);
        crmResponse.put("failedRecords", failedRecords);

        if (updatedRecords.isEmpty() || !errors.isEmpty() || !failedRecords.isEmpty()) {
            crmResponse.setErrorCode(ErrorCode.get(CommonErrorCodes.UPDATE_ACTION_FAILED));
        }
    }

    /**
     * <p>
     * Deletes the given entity
     * </p>
     *
     * @param model
     */
    public void delete(final CrmResponse crmResponse, final T model) {
        delete(crmResponse, model.orgId(), List.of(model));
    }

    /**
     * <p>
     * Deletes the given entities
     * </p>
     *
     * @param models
     * @return
     */
    public void delete(final CrmResponse crmResponse, final Long orgId, final Collection<T> models) {
        final Collection<String> errors = validate(models, Delete.class);

        if (errors.isEmpty()) {
            getConfigManager().delete(orgId, models);
        }

        crmResponse.put("errors", errors);
        if (!errors.isEmpty()) {
            crmResponse.setErrorCode(ErrorCode.get(CommonErrorCodes.DELETE_ACTION_FAILED));
        }
    }

    /**
     * <p>
     * Checks the existence of an entity
     * </p>
     *
     * @param model
     * @return
     */
    public boolean exists(final T model) {
        return getConfigManager().exists(model);
    }

    /**
     * <p>
     * Partially fetches the values of an entity
     * </p>
     *
     * @param orgId
     * @param id
     * @return
     */
    public T partialGet(final Long orgId, final Long id) {
        return getConfigManager().partialGet(orgId, id);
    }

    /**
     * <p>
     * Fetches an entity by its id and organization id
     * </p>
     *
     * @param orgId
     * @param id
     * @return
     */
    public T get(final Long orgId, final Long id) {
        return getConfigManager().get(orgId, id);
    }

    /**
     * <p>
     * Fetches the entities by its ids and organization id
     * </p>
     *
     * @param orgId
     * @param ids
     * @return
     */
    public Collection<T> get(final Long orgId, final Collection<Long> ids) {
        return getConfigManager().get(orgId, ids);
    }

    /**
     * <p>
     * Fetches the CRM config manager
     * </p>
     *
     * @return
     */
    public abstract AbstractConfigManager<T> getConfigManager();

    /**
     * <p>
     * Converts the {@link JsonObject} into subclass of {@link TlcModel}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public abstract T convert(final Long orgId, final JsonObject jsonObject);

    /**
     * <p>
     * Converts the {@link JsonArray} into collection of subclass of {@link TlcModel}
     * </p>
     *
     * @param orgId
     * @param jsonArray
     * @return
     */
    public Collection<T> convert(final Long orgId, final JsonArray jsonArray) {
        Collection<T> models = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            models.add(convert(orgId, jsonArray.getJsonObject(i)));
        }

        return models;
    }

    /**
     * <p>
     * Converts subclass of {@link TlcModel} into {@link JsonObject}
     * </p>
     *
     * @param t
     * @return {@link JsonObject}
     */
    public abstract JsonObject convert(final T t);

    /**
     * <p>
     * Converts collection of subclass of {@link TlcModel} into {@link JsonArray}
     * </p>
     *
     * @param models
     * @return {@link JsonArray}
     */
    public JsonArray convert(final Collection<T> models) {
        final JsonArray array = Json.array();

        models.stream().forEach(model -> array.put(convert(model)));

        return array;
    }

    /**
     * <p>
     * Converts partial data into {@link TlcModel}
     * </p>
     *
     * @param orgId
     * @param jsonObject
     * @return
     */
    public abstract T convertPartialData(final Long orgId, final JsonObject jsonObject);

    /**
     * <p>
     * Validates the models against the given validator groups
     * </p>
     *
     * @param models
     * @param validatorGroups
     */
    private Collection<String> validate(Collection<T> models, Class... validatorGroups) {
        final ModelValidator validator = ValidatorAccess.get();
        final Set<ConstraintViolation<T>> violations = new HashSet<>();

        models.stream().forEach(model -> violations.addAll(validator.validate(model, validatorGroups)));

        return violations.stream().map(violation ->
                violation.getMessage()).collect(Collectors.toSet());
    }

}
