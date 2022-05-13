package com.tlc.crm.common.action.secure.list;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.action.secure.CrmSecureAction;
import com.tlc.i18n.I18nKey;
import com.tlc.i18n.I18nResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmListAction extends CrmSecureAction
{
    private static final String PROVIDER = "provider";
    private static final String SELECT = "select";
    private static final String SORT = "sort";
    private static final String SORT_DESC = "desc";
    private static final String CRITERIA = "criteria";
    private static final String LIMIT = "limit";

    private static final String FETCH_HITS = "fetchHits";
    private static final String FETCH_STRUCTURE = "fetchStruct";

    private static final String KEY = "field";
    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final String START = "start";
    private static final String COUNT = "count";

    @Override
    public final void sProcess(CrmRequest request, CrmResponse crmResponse)
    {
        final I18nResolver i18nResolver = request.i18nResolver();
        final JsonObject requestJson = request.getRequestJson();
        final String provider = requestJson.getString(PROVIDER);
        final ListInterpreter listInterpreter = getListInterpreter(request, provider, i18nResolver);

        preProcess(listInterpreter);

        final JsonArray selectFields = requestJson.optJsonArray(SELECT);
        if (selectFields != null)
        {
            final int size = selectFields.size();
            for(int index = 0; index < size; index++)
            {
                final String field = selectFields.getString(index);
                listInterpreter.selectClause(field);
            }
        }
        else
        {
            listInterpreter.applyDefaultSelect();
        }

        final JsonArray sortFields = requestJson.optJsonArray(SORT);
        if (sortFields != null)
        {
            final int size = sortFields.size();
            for(int index = 0; index < size; index++)
            {
                final JsonObject fieldInfo = sortFields.getJsonObject(index);
                final String field = fieldInfo.getString(KEY);
                final Object sortType = fieldInfo.get(TYPE);
                listInterpreter.orderByClause(field, sortType == null || !sortType.toString().equalsIgnoreCase(SORT_DESC));
            }
        }
        else
        {
            listInterpreter.applyDefaultOrderBy();
        }

        final JsonArray criteria = requestJson.optJsonArray(CRITERIA);
        if (criteria != null)
        {
            final int size = criteria.size();
            for (int index = 0; index < size; index++)
            {
                final JsonObject criteriaJson = criteria.getJsonObject(index);
                final String field = criteriaJson.getString(KEY);
                final Object value = criteriaJson.get(VALUE);
                final String type = criteriaJson.getString(TYPE);
                if(value instanceof JsonArray)
                {
                    listInterpreter.criteria(field, ((JsonArray)value).toList(), type);
                }
                else
                {
                    listInterpreter.criteria(field, value, type);
                }
            }
        }
        final JsonObject limitInfo = requestJson.optJsonObject(LIMIT);
        if (limitInfo != null)
        {
            final int start = requestJson.getInt(START);
            final int count = requestJson.getInt(COUNT);
            listInterpreter.setLimits(start, count);
        }
        else
        {
            listInterpreter.applyDefaultLimit();
        }

        final JsonObject response = Json.object();
        if(requestJson.optBoolean(FETCH_HITS))
        {
            final long totalHits = listInterpreter.fetchTotalHits();
            response.put("totalHits", totalHits);
            if(totalHits > 0)
            {
                final JsonArray rows = fetchContent(listInterpreter, i18nResolver);
                response.put("rows", rows);
            }
        }
        else
        {
            final JsonArray rows = fetchContent(listInterpreter, i18nResolver);
            response.put("rows", rows);
        }

        if (requestJson.optBoolean(FETCH_STRUCTURE))
        {
            final JsonObject structureJson = fetchStructure(listInterpreter, i18nResolver);
            response.put("structure", structureJson);
        }

        crmResponse.put("table", response);
    }

    public void preProcess(ListInterpreter listInterpreter)
    {
    }

    public ListRowListener getRowListener()
    {
        return null;
    }

    public ListInterpreter getListInterpreter(CrmRequest request, String provider, I18nResolver i18nResolver)
    {
        final Long userId = request.userId();
        return new SQLListInterpreter(userId, provider, i18nResolver);
    }

    private JsonArray fetchContent(ListInterpreter listInterpreter, I18nResolver i18nResolver)
    {
        final String primaryField = listInterpreter.primaryField();
        final ListStructure structure = listInterpreter.structure();
        final Collection<Map<String, Object>> result = listInterpreter.getResult();

        final ListRowListener rowListener = getRowListener();
        final JsonArray converted = Json.array();
        for (Map<String, Object> row : result)
        {
            final Object pk = row.get(primaryField);
            final JsonObject convertedRow = Json.object();
            row.forEach((key, value) ->
            {
                final Object orgValue = value instanceof I18nKey i18n ?
                        (i18nResolver == null ? i18n.getKey() : i18nResolver.get(i18n)) : value;

                final Object convertedValue;
                final Transformer transformer = structure.getTransformer(key);
                if (transformer != null)
                {
                    convertedValue = transformer.transform(orgValue, row);
                    convertedRow.put(key, convertedValue);
                }
                else
                {
                    convertedRow.put(key, orgValue);
                }
            });
            if (rowListener != null)
            {
                rowListener.call(pk, convertedRow);
            }

            converted.put(convertedRow);
        }
        return converted;
    }

    private JsonObject fetchStructure(ListInterpreter listInterpreter, I18nResolver i18n)
    {
        final ListStructure listStructure = listInterpreter.structure();
        final JsonObject structureJson = Json.object();
        structureJson.put("primary", listInterpreter.primaryField());

        final List<Field> fields = listStructure.getFields();
        final JsonArray fieldsArray = Json.array();
        for (Field field : fields)
        {
            final JsonObject fieldInfoObject = Json.object();
            fieldInfoObject.put("name", field.name());
            final I18nKey displayName = field.displayName();
            fieldInfoObject.put("displayName", i18n == null ? displayName.getKey() : i18n.get(displayName));
            fieldInfoObject.put("sort", field.sort());
            fieldInfoObject.put("search", field.search());
            fieldInfoObject.put("config", field.config());
            fieldInfoObject.put("hidden", field.hidden());
            fieldsArray.put(fieldInfoObject);
        }
        structureJson.put("fields", fieldsArray);
        final List<String> configured = listInterpreter.configuredFields();
        structureJson.put("configured", configured);

        return structureJson;
    }
}
