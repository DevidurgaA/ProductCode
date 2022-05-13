package com.tlc.sql.api.dml;


/**
 * @author Abishek
 * @version 1.0
 */
public interface Query
{
    void setWhereClause(WhereClause criteria);

    WhereClause getWhereClause();
    
    Table getBaseTable();
}
