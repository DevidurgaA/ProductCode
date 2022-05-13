package com.tlc.crm.common.action.secure.rest;

import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.AbstractCrmAction;
import com.tlc.crm.common.action.CrmResponse;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmRestAction extends AbstractCrmAction
{
    @Override
    public final void process(CrmRequest request, CrmResponse crmResponse) throws Exception
    {
        rProcess(request, crmResponse);
    }

    public abstract void rProcess(CrmRequest request, CrmResponse crmResponse) throws Exception;
}
