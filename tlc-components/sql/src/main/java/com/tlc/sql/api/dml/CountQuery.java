package com.tlc.sql.api.dml;

import com.tlc.sql.internal.dml.CountQueryImpl;


/**
 * @author Abishek
 * @version 1.0
 */
public interface CountQuery extends MultiQuery
{
	static CountQuery get(Table table)
	{
		return new CountQueryImpl(table);
	}
}

