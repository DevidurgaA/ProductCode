package com.tlc.sql.internal.data;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Column;
import com.tlc.sql.api.dml.Criteria;
import com.tlc.sql.api.dml.Operator;
import com.tlc.sql.api.dml.WhereClause;
import com.tlc.sql.api.meta.DataType;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Abishek
 * @version 1.0
 */
public class DataContainerFilter implements Predicate<Row>
{
    private final WhereClause whereClause;
    DataContainerFilter(WhereClause whereClause)
    {
        this.whereClause = Objects.requireNonNull(whereClause);
    }

    @Override
    public boolean test(Row row)
    {
        return match(whereClause, row);
    }

    private boolean match(WhereClause whereClause, Row row)
    {
        if(whereClause.isConnector())
        {

            final WhereClause left = whereClause.getLeft();
            final WhereClause right = whereClause.getRight();
            final WhereClause.Connector connector = whereClause.getConnector();
            if(connector == WhereClause.Connector.AND)
            {
                return match(left, row) && match(right, row) ;
            }
            else
            {
                return match(left, row) || match(right, row);
            }

        }
        else
        {
            final Criteria sqlCriteria = whereClause.getCriteria();
            final Column column = sqlCriteria.getColumn();
            final Object criData = sqlCriteria.getValue();
            final Operator operator = sqlCriteria.getOperator();

            final Object data = row.get(column);
            final DataType dataType = column.getColumnDefinition().getDataType();

            if(operator == Operator.EQUAL)
            {
                return dataType.equals(data, criData);
            }
            else if(operator == Operator.NOT_EQUAL)
            {
                return !dataType.equals(data, criData);
            }
            else if(operator == Operator.IN || operator == Operator.NOT_IN)
            {
                if(data == null)
                {
                    return false;
                }
                if(!(criData instanceof Collection))
                {
                    throw ErrorCode.get(SQLErrorCodes.DB_QUERY_INVALID_CRITERIA_INPUT);
                }
                if(operator == Operator.IN)
                {
                    return ((Collection<?>)criData).stream().anyMatch(s-> dataType.equals(s, data));
                }
                else
                {
                    return ((Collection<?>)criData).stream().noneMatch(s-> dataType.equals(s, data));
                }
            }
            else
            {
                throw ErrorCode.get(ErrorCodes.NOT_SUPPORTED);
            }
        }
    }
}
