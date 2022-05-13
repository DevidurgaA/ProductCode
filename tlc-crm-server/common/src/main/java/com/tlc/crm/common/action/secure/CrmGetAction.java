package com.tlc.crm.common.action.secure;

import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.config.AbstractConfigManager;
import com.tlc.validator.TlcModel;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmGetAction<T extends TlcModel> extends CrmSecureAction
{
    @Override
    public final void sProcess(CrmRequest request, CrmResponse crmResponse)
    {
        final JsonObject requestJson = request.getRequestJson();
        final JsonObject data = requestJson.getJsonObject("data");
        final Long id = data.getLong("id");
        final Long orgId = request.orgId();

        final T model = get(orgId, id);

        crmResponse.put("data", construct(model));
    }

    public T get(final Long orgId, final Long id)
    {
        return getConfigManager().get(orgId, id);
    }

    public abstract AbstractConfigManager<T> getConfigManager();

    public abstract JsonObject construct(T model);
}
