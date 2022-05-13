package com.tlc.sql.internal.ds;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.*;
import com.tlc.sql.api.ds.OrgDataStore;
import com.tlc.sql.internal.handler.DMLHandler;
import com.tlc.sql.api.meta.ImmutableColumns;
import com.tlc.sql.api.meta.TableType;

import java.util.Collection;
import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class OrgDataStoreImpl extends WritableDataStoreImpl implements OrgDataStore
{
    private final Long orgId;
    private final String orgIdColumn;
    public OrgDataStoreImpl(Long orgId, DsProvider dsProvider, DMLHandler dmlHandler)
    {
        super(dsProvider, dmlHandler);
        this.orgId = Objects.requireNonNull(orgId);
        this.orgIdColumn = ImmutableColumns.ORG_ID.getName();
    }

    @Override
    public Long getOrgId()
    {
        return orgId;
    }

    @Override
    protected Row wrapRow(Row row)
    {
        final TableType tableType = row.getTable().getTableDefinition().getType();
        if(tableType.isOrgDependent())
        {
            if(row.isNewRow())
            {
                row.setOrgId(orgId);
            }
            else
            {
                final Long exOrgId = row.getOrgId();
                if(exOrgId == null || !exOrgId.equals(orgId))
                {
                    throw ErrorCode.getLite(ErrorCodes.ACCESS_DENIED);
                }
            }
        }
        return row;
    }

    @Override
    protected SelectQuery wrapSelectQuery(SelectQuery selectQuery)
    {
        wrapMultiQuery(selectQuery);
        return selectQuery;
    }

    @Override
    protected CountQuery wrapCountQuery(CountQuery countQuery)
    {
        wrapMultiQuery(countQuery);
        return countQuery;
    }

    @Override
    protected UpdateQuery wrapUpdateQuery(UpdateQuery updateQuery)
    {
        wrapQuery(updateQuery);
        return updateQuery;
    }

    @Override
    protected DeleteQuery wrapDeleteQuery(DeleteQuery deleteQuery)
    {
        wrapQuery(deleteQuery);
        return deleteQuery;
    }

    private void wrapMultiQuery(MultiQuery query)
    {
        final Collection<Table> tables = query.getTables();
        WhereClause builder = null;
        for (Table table : tables)
        {
            final TableType tableType = table.getTableDefinition().getType();
            if(tableType.isOrgDependent())
            {
                WhereClause oldBuilder = new WhereClause(Criteria.eq(table.getColumn(orgIdColumn), orgId));
                if(tableType.isCommonNullExists())
                {
                    oldBuilder = oldBuilder.or(Criteria.eq(table.getColumn(orgIdColumn), null));
                }
                builder = null == builder ? oldBuilder : oldBuilder.and(builder);
            }
        }
        if(builder != null)
        {
            final WhereClause existing = query.getWhereClause();
            query.setWhereClause(builder.and(existing));
        }
    }

    private void wrapQuery(Query query)
    {
        final Table table = query.getBaseTable();
        final TableType tableType = table.getTableDefinition().getType();
        if(tableType.isOrgDependent())
        {
            final WhereClause criteriaBuilder = query.getWhereClause();
            final WhereClause orgCriteria = new WhereClause(Criteria.eq(table.getColumn(orgIdColumn), orgId));
            query.setWhereClause(orgCriteria.and(criteriaBuilder));
        }
    }
}
