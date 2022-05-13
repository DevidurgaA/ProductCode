package com.tlc.sql.internal.dml;

import com.tlc.sql.api.dml.CountQuery;
import com.tlc.sql.api.dml.Table;


/**
 * @author Abishek
 * @version 1.0
 */
public class CountQueryImpl extends MultiQueryImpl implements CountQuery
{
	public CountQueryImpl(Table table)
	{
		super(table);
	}
}
