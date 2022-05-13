package com.tlc.crm.common.action.pub;

import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.AbstractCrmAction;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmPublicAction extends AbstractCrmAction
{
    @Override
    public final void process(CrmRequest request, CrmResponse crmResponse) throws Exception
    {
        pProcess(request, crmResponse);
    }

    public abstract void pProcess(CrmRequest request, CrmResponse crmResponse) throws Exception;
}