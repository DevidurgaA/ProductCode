package com.tlc.sql.api.dml;



/**
 * @author Abishek
 * @version 1.0
 */
public class LimitClause
{
	private final int start;
	private final int numberOfResults;
	
	public LimitClause(int limit)
	{
		this(1, limit);
	}
	
	public LimitClause(int start, int numberOfResults)
	{
		this.start = start;
		this.numberOfResults = numberOfResults;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getNumberOfResults()
	{
		return numberOfResults;
	}
}
