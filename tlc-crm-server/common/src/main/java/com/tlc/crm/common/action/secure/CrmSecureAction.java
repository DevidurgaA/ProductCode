package com.tlc.crm.common.action.secure;

import com.tlc.commons.json.JsonObject;
import com.tlc.crm.common.action.AbstractCrmAction;
import com.tlc.crm.common.action.CrmRequest;
import com.tlc.crm.common.action.CrmResponse;

/**
 * @author Abishek
 * @version 1.0
 */
public abstract class CrmSecureAction extends AbstractCrmAction
{
    @Override
    public final void process(CrmRequest request, CrmResponse crmResponse) throws Exception
    {
        //TODO authentication check
        sProcess(request, crmResponse);
    }

    public abstract void sProcess(CrmRequest request, CrmResponse crmResponse) throws Exception;
}
