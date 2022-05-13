package com.tlc.crm.common.action.secure.list;

import com.tlc.i18n.I18nKey;

/**
 * @author Abishek
 * @version 1.0
 */
record Field(Long id, String name, I18nKey displayName, boolean primary, int sort, boolean search,
             boolean hidden, String config, FieldSource source, Transformer transformer)
{

}

record FieldSource(String table,
                   String column, int type, int flag)
{

}
