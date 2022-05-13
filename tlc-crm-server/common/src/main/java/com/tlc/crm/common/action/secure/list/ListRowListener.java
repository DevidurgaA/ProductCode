package com.tlc.crm.common.action.secure.list;

import com.tlc.commons.json.JsonObject;

/**
 * @author Abishek
 * @version 1.0
 */
public interface ListRowListener
{
    void call(Object key, JsonObject row);
}
