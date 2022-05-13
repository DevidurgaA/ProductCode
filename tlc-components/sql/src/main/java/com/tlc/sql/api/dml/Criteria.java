package com.tlc.sql.api.dml;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class Criteria
{
    protected final Column column;
    protected final Object value;
    protected final Operator operator;

    public Criteria(Column column, Object value, Operator operator)
    {
        this.column = Objects.requireNonNull(column);
        this.value = value;
        this.operator = Objects.requireNonNull(operator);
    }

    public Column getColumn()
    {
        return column;
    }

    public Object getValue()
    {
        return value;
    }

    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public String toString()
    {
        return column + " " + operator + " " + value;
    }

    private static Criteria create(Column column, Object value, Operator operator)
    {
        final DataType dataType = column.getColumnDefinition().getDataType();
        if(dataType.isStorageType())
        {
            throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_COLUMN);
        }
        return new Criteria(column, value, operator);
    }

    public static Criteria eq(Column column, Object value)
    {
        return create(column, value, Operator.EQUAL);
    }

    public static Criteria bitAnd(Column column, int value, int result)
    {
        return create(column, new Number[]{value, result}, Operator.BIT_AND);
    }

    public static Criteria notEq(Column column, Object value)
    {
        return create(column, value, Operator.NOT_EQUAL);
    }

    public static Criteria startsWith(Column column, String value)
    {
        return create(column, value, Operator.STARTS_WITH);
    }

    public static Criteria notStartsWith(Column column, String value)
    {
        return create(column, value, Operator.NOT_STARTS_WITH);
    }

    public static Criteria contains(Column column, String value)
    {
        return create(column, value, Operator.CONTAINS);
    }

    public static Criteria notContains(Column column, Object value)
    {
        return create(column, value, Operator.NOT_CONTAINS);
    }

    public static Criteria in(Column column, Collection<?> value)
    {
        return create(column, value, Operator.IN);
    }

    public static Criteria in(Column column, SelectQuery selectQuery)
    {
        return create(column, selectQuery, Operator.IN);
    }

    public static Criteria notIn(Column column, Collection<?>  value)
    {
        return create(column, value, Operator.NOT_IN);
    }

    public static Criteria notIn(Column column, SelectQuery selectQuery)
    {
        return create(column, selectQuery, Operator.IN);
    }

    public static Criteria ge(Column column, Number value)
    {
        return create(column, value, Operator.GREATER_EQUAL);
    }

    public static Criteria le(Column column, Number value)
    {
        return create(column, value, Operator.LESS_EQUAL);
    }

    public static Criteria lt(Column column, Number value)
    {
        return create(column, value, Operator.LESS_THAN);
    }

    public static Criteria gt(Column column, Number value)
    {
        return create(column, value, Operator.GREATER_THAN);
    }

    public static Criteria between(Column column, Number start, Number end)
    {
        return create(column, new Number[]{start, end}, Operator.BETWEEN);
    }

    public static Criteria notBetween(Column column, Number start, Number end)
    {
        return create(column, new Number[]{start, end}, Operator.NOT_BETWEEN);
    }
}
