package com.tlc.crm.common.action.secure.list;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.i18n.I18nResolver;
import com.tlc.sql.SQLAccess;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.sql.api.meta.DataType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Abishek
 * @version 1.0
 */
public class SQLListInterpreter implements ListInterpreter
{
    private static final int SORT_DISABLED = 0;
    private static final int SORT_ENABLED = 1;
    private static final int SORT_DEFAULT_ASCENDING = 2;
    private static final int SORT_DEFAULT_DESCENDING = 3;

    private static final int SELECT_DISTINCT = 1;
    private static final int SELECT_COUNT = 2;
    private static final int SELECT_MIN = 3;
    private static final int SELECT_MAX = 4;
    private static final int SELECT_AVG = 5;

//    private final I18nResolver i18nResolver;
    private final ListStructure listStructure;
    private final String providerName;
    private final Long userId;
    private final SelectQuery selectQuery;
    private final I18nResolver i18nResolver;

    private List<Field> configuredFields = null;
    public SQLListInterpreter(Long userId, String providerName, I18nResolver i18nResolver)
    {
//        this.i18nResolver = Objects.requireNonNull(i18nResolver);
        this.providerName = Objects.requireNonNull(providerName);
        this.userId = Objects.requireNonNull(userId);
        this.i18nResolver = i18nResolver;
        this.listStructure = StructureHandler.get().getStructure(userId, providerName);

        final List<Field> fields = listStructure.getFields();
        final SortedSet<Table> tables = new TreeSet<>();
        for (Field field : fields)
        {
            final FieldSource fieldSource = field.source();
            final Table table = Table.get(fieldSource.table());
            tables.add(table);
        }
        final List<JoinClause> joinClauses = DmlUtil.getJoins(tables);
        this.selectQuery = SelectQuery.get(tables.first());
        selectQuery.addJoinClause(joinClauses);
    }

    @Override
    public void applyDefaultOrderBy()
    {
        final List<Field> fields = listStructure.getFields();
        for (Field field : fields)
        {
            final int sortFlag = field.sort();
            if(sortFlag == SORT_DEFAULT_ASCENDING || sortFlag == SORT_DEFAULT_DESCENDING)
            {
                final OrderByClause orderByClause = getOrderByClause(field);
                selectQuery.addOrderByClause(orderByClause);
            }
        }
    }

    @Override
    public void applyDefaultSelect()
    {
        final List<Field> fields = getConfiguredFieldsInternal();
        for (Field field : fields)
        {
            final Column column = getSelectColumn(field);
            selectQuery.addSelectClause(column);
        }
    }

    @Override
    public void applyDefaultLimit()
    {
        selectQuery.setLimitClause(new LimitClause(1, 10));
    }

    @Override
    public void selectClause(String fieldName)
    {
        final Field field = listStructure.getField(fieldName);
        final Column column = getSelectColumn(field);
        selectQuery.addSelectClause(column);
    }

    @Override
    public void orderByClause(String fieldName, boolean asc)
    {
        final Field field = listStructure.getField(fieldName);
        if(field.sort() != SORT_DISABLED)
        {
            final OrderByClause orderByClause = getOrderByClause(field, asc);
            selectQuery.addOrderByClause(orderByClause);
        }
        else
        {
            throw ErrorCode.get(ErrorCodes.ACCESS_DENIED, "Sort not allowed for this field!!");
        }
    }

    @Override
    public void criteria(String fieldName, Object value, String condition)
    {
        final Field field = listStructure.getField(fieldName);
        if(field.search())
        {
            final FieldSource sqlSource = field.source();
            final Table table = Table.get(sqlSource.table());
            final Column column = table.getColumn(sqlSource.column(), field.name());
            final DataType dataType = column.getColumnDefinition().getDataType();
            switch (condition)
            {
                case "contains" ->{
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.contains(column, value.toString(), i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.contains(column, value.toString())));
                    }
                }
                case "startsWith" -> {
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.startsWith(column, value.toString(), i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.startsWith(column, value.toString())));
                    }
                }
                case "in" -> {
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.in(column, (Collection<String>) value, i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.in(column, (Collection<?>) value)));
                    }
                }
                case "notIn" -> {
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.notIn(column, (Collection<String>) value, i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.notIn(column, (Collection<?>) value)));
                    }
                }
                case "notEqual" -> {
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.notEq(column, value.toString(), i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.notEq(column, value)));
                    }
                }
                default -> {
                    if(dataType == DataType.KCHAR)
                    {
                        appendCondition(KCharCriteria.eq(column, value.toString(), i18nResolver));
                    }
                    else
                    {
                        appendCondition(new WhereClause(Criteria.eq(column, value)));
                    }
                }
            }
        }
        else
        {
            throw ErrorCode.get(ErrorCodes.ACCESS_DENIED, "Search not allowed for this field!!");
        }
    }

    @Override
    public void setLimits(int start, int limit)
    {
        selectQuery.setLimitClause(new LimitClause(start, Math.min(100, limit)));
    }

    @Override
    public long fetchTotalHits()
    {
        final CountQuery countQuery = CountQuery.get(selectQuery.getBaseTable());
        countQuery.setWhereClause(selectQuery.getWhereClause());
        countQuery.addJoinClause(selectQuery.getJoinClause());
        final OrgDataStore orgDataStore = SQLAccess.get().getOrgDataStore(userId);
        return orgDataStore.get(countQuery);
    }

    @Override
    public Collection<Map<String, Object>> getResult()
    {
        final OrgDataStore orgDataStore = SQLAccess.get().getOrgDataStore(userId);
        return orgDataStore.getData(selectQuery);
    }

    @Override
    public String primaryField()
    {
        return listStructure.getPrimaryField().name();
    }

    @Override
    public List<String> configuredFields()
    {
        final List<Field> fields = getConfiguredFieldsInternal();
        return fields.stream().map(Field::name).collect(Collectors.toList());
    }

    @Override
    public ListStructure structure()
    {
        return listStructure;
    }

    @Override
    public void updateConfiguredFields(List<String> fields)
    {
        final StructureHandler structureHandler = StructureHandler.get();
        final List<Field> fieldsList = fields.stream().map(listStructure::getField).collect(Collectors.toList());
        structureHandler.updateConfiguredFields(userId, listStructure.getProviderId(), fieldsList);
    }

    private List<Field> getConfiguredFieldsInternal()
    {
        if(configuredFields == null)
        {
            final StructureHandler structureHandler = StructureHandler.get();
            this.configuredFields = structureHandler.fetchConfiguredFields(userId, providerName);
        }
        return configuredFields;
    }

    private OrderByClause getOrderByClause(Field field)
    {
        return getOrderByClause(field, field.sort() == SORT_DEFAULT_ASCENDING);
    }

    private OrderByClause getOrderByClause(Field field, boolean asc)
    {
        final FieldSource sqlSource = field.source();
        final Table table = Table.get(sqlSource.table());
        final Column column = table.getColumn(sqlSource.column());
        return new OrderByClause(column, asc ? OrderByClause.OrderType.ASCENDING : OrderByClause.OrderType.DESCENDING);
    }

    private Column getSelectColumn(Field field)
    {
        final FieldSource sqlSource = field.source();
        final Table table = Table.get(sqlSource.table());
        final Column column = table.getColumn(sqlSource.column(), field.name());
        return  switch (sqlSource.type())
                {
                    case SELECT_DISTINCT -> column.distinct();
                    case SELECT_COUNT -> column.count();
                    case SELECT_MIN -> column.min();
                    case SELECT_MAX -> column.max();
                    case SELECT_AVG -> column.average();
                    default -> column;
                };
    }

    private void appendCondition(WhereClause whereClause)
    {
        final WhereClause existing = selectQuery.getWhereClause();
        if (existing == null)
        {
            selectQuery.setWhereClause(whereClause);
        }
        else
        {
            selectQuery.setWhereClause(existing.and(whereClause));
        }
    }
}
