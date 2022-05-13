package com.tlc.sql.internal.dml;

import com.tlc.sql.api.dml.DeleteQuery;
import com.tlc.sql.api.dml.Table;


/**
 * @author Abishek
 * @version 1.0
 */
public class DeleteQueryImpl extends QueryImpl implements DeleteQuery
{
    public DeleteQueryImpl(Table table)
    {
        super(table);
    }
}
