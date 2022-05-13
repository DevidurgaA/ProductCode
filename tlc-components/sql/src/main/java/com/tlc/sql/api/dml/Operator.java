package com.tlc.sql.api.dml;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;

/**
 * @author Abishek
 * @version 1.0
 */

public enum Operator
{
    /*
     *  Common Conditions
     */
    EQUAL(1), NOT_EQUAL(2), IN(3), NOT_IN(4),

    /*
	 *  Number Related Conditions
	 */
	GREATER_EQUAL(101), LESS_EQUAL(102), LESS_THAN(103), GREATER_THAN(104), BETWEEN(105), NOT_BETWEEN(106), BIT_AND(107), BIT_OR(108),
	/*
	 *  String Related Conditions
	 */
	STARTS_WITH(201), NOT_STARTS_WITH(202), ENDS_WITH(203), NOT_ENDS_WITH(204), CONTAINS(205), NOT_CONTAINS(206), LIKE(207), REGEX(208),

	/*
		Special Conditions
	 */
	EXISTS(301),

	;
	private final int id;
	Operator(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public static Operator getOperator(String name)
	{
		return switch (name) {
			case "equal" -> EQUAL;
			case "notEqual" -> NOT_EQUAL;
			case "in" -> IN;
			case "gte" -> GREATER_EQUAL;
			case "lte" -> LESS_EQUAL;
			case "gt" -> GREATER_THAN;
			case "lt" -> LESS_THAN;
			default -> throw ErrorCode.get(ErrorCodes.UNKNOWN_INPUT, name);
		};
	}
}

