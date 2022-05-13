package com.tlc.sql.api.dml;

import com.tlc.i18n.I18nResolver;
import com.tlc.sql.api.meta.DataType;

import java.util.Collection;
import java.util.List;

/**
 * @author Abishek
 * @version 1.0
 */
public class KCharCriteria
{
    public static WhereClause eq(Column column, String value, I18nResolver i18nResolver)
    {
        return in(column, List.of(value), i18nResolver);
    }

    public static WhereClause in(Column column, Collection<String> values, I18nResolver i18nResolver)
    {
        if(i18nResolver != null && values != null && !values.isEmpty())
        {
            if(column.getColumnDefinition().getDataType() == DataType.KCHAR)
            {
                final Collection<String> keyValueList = i18nResolver.getKeysEqualsValues(column.getTableName(), values);
                final WhereClause whereClause = new WhereClause(Criteria.in(column, values));
                if(!keyValueList.isEmpty())
                {
                    return whereClause.or(Criteria.in(column, keyValueList));
                }
                else
                {
                    return whereClause;
                }
            }
        }
        return new WhereClause(Criteria.eq(column, values));
    }

    public static WhereClause notEq(Column column, String value, I18nResolver i18nResolver)
    {
        return notIn(column, List.of(value), i18nResolver);
    }

    public static WhereClause notIn(Column column, Collection<String> values, I18nResolver i18nResolver)
    {
        if(i18nResolver != null && values != null && !values.isEmpty())
        {
            if(column.getColumnDefinition().getDataType() == DataType.KCHAR)
            {
                final Collection<String> keyValueList = i18nResolver.getKeysEqualsValues(column.getTableName(),values);
                final WhereClause whereclause = new WhereClause(Criteria.notIn(column, values));
                if(!keyValueList.isEmpty())
                {
                    return whereclause.and(Criteria.notIn(column, keyValueList));
                }
                else
                {
                    return whereclause;
                }
            }
        }
        return new WhereClause(Criteria.notEq(column, values));
    }

    public static WhereClause startsWith(Column column, String value, I18nResolver i18nResolver)
    {
        if(i18nResolver != null && value != null && !value.isEmpty())
        {
            if(column.getColumnDefinition().getDataType() == DataType.KCHAR)
            {
                final Collection<String> keyValueList = i18nResolver.getKeysStartsWithValues(column.getTableName(), List.of(value));
                final WhereClause whereclause = new WhereClause(Criteria.startsWith(column, value));
                if(!keyValueList.isEmpty())
                {
                    return whereclause.or(Criteria.in(column, keyValueList));
                }
                else
                {
                    return whereclause;
                }
            }
        }
        return new WhereClause(Criteria.startsWith(column, value));
    }

    public static WhereClause contains(Column column, String value, I18nResolver i18nResolver)
    {
        if(i18nResolver != null && value != null && !value.isEmpty())
        {
            if(column.getColumnDefinition().getDataType() == DataType.KCHAR)
            {
                final Collection<String> keyValueList = i18nResolver.getKeysContainsValues(column.getTableName(), List.of(value));
                final WhereClause whereclause = new WhereClause(Criteria.contains(column, value));
                if(!keyValueList.isEmpty())
                {
                    return whereclause.or(Criteria.in(column, keyValueList));
                }
                else
                {
                    return whereclause;
                }
            }
        }
        return new WhereClause(Criteria.contains(column, value));
    }
}