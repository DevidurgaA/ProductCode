package com.tlc.crm.common.action.secure.list;

import java.util.Map;

public interface Transformer
{
    Object transform(Object data, Map<String, Object> allData);
}
