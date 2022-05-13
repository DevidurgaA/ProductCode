package com.tlc.crm.common.action.secure.list;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */
public interface ListInterpreter
{
    void applyDefaultOrderBy();

    void applyDefaultSelect();

    void applyDefaultLimit();

    void selectClause(String field);

    void orderByClause(String field, boolean isAsc);

    void criteria(String key, Object value, String condition);

    void setLimits(int start, int limit);

    long fetchTotalHits();

    Collection<Map<String, Object>> getResult();

    String primaryField();

    List<String> configuredFields();

    ListStructure structure();

    void updateConfiguredFields(List<String> fields);
}
