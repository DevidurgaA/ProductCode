package com.tlc.sql.api.meta;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.i18n.I18nKey;

import java.sql.Types;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public enum DataType
{
	CHAR("CHAR", Types.VARCHAR),
	SCHAR("SCHAR", Types.VARCHAR),
	KCHAR("KCHAR", Types.VARCHAR),

	SMALLINT("SMALLINT", Types.INTEGER),
	INTEGER("INTEGER", Types.INTEGER),
	BIGINT("BIGINT", Types.BIGINT),
	DECIMAL("DECIMAL", Types.DECIMAL),

	BOOLEAN("BOOLEAN", Types.BOOLEAN),

	BLOB("BLOB", Types.BLOB),
	TEXT("TEXT", Types.VARCHAR),
	STEXT("STEXT", Types.VARCHAR);


	private final String dataType;
	private final int sqlType;
	DataType(String dataType, int sqlType)
	{
		this.sqlType = sqlType;
		this.dataType = Objects.requireNonNull(dataType);
	}

	public String getDataTypeStr()
	{
		return dataType;
	}

	public int getSqlType()
	{
		return sqlType;
	}

	public boolean isNumeric()
	{
		return this == SMALLINT || this == INTEGER || this == BIGINT || this == DECIMAL;
	}

	public boolean isString()
	{
		return this == CHAR || this == KCHAR || this == SCHAR || this == TEXT || this == STEXT;
	}

	public boolean isStorageType()
	{
		return this == TEXT || this == STEXT || this == BLOB;
	}

	public boolean canConvertTo(DataType dataType)
	{
		return (this == SMALLINT && (dataType == INTEGER || dataType == BIGINT))
				|| (this == INTEGER && dataType == BIGINT);
	}

	public Object getWrappedValue(Object value)
	{
		return getValue(value, true);
	}

	public Object getUnWrappedValue(Object value)
	{
		return getValue(value, false);
	}

	public boolean isEqual(Object one, Object two)
	{
		if(one == null && two == null)
		{
			return true;
		}
		else if(one != null && two != null)
		{
			switch(this)
			{
				case CHAR:
				case SCHAR:
				case TEXT:
				case STEXT:
				{
					final String first = (one instanceof String) ? (String) one : one.toString();
					final String second = (two instanceof String) ? (String) two : two.toString();
					return first.equals(second);
				}
				case SMALLINT:
				{
					final Short first = (one instanceof Short) ? (Short) one : Short.parseShort(one.toString());
					final Short second = (two instanceof Short) ? (Short) two : Short.parseShort(two.toString());
					return first.equals(second);
				}
				case INTEGER:
				{
					final Integer first = (one instanceof Integer) ? (Integer) one : Integer.parseInt(one.toString());
					final Integer second = (two instanceof Integer) ? (Integer) two : Integer.parseInt(two.toString());
					return first.equals(second);
				}
				case BIGINT:
				{
					final Long first = (one instanceof Long) ? (Long) one : Long.parseLong(one.toString());
					final Long second = (two instanceof Long) ? (Long) two : Long.parseLong(two.toString());
					return first.equals(second);
				}
				case DECIMAL: {
					final Double first = (one instanceof Double) ? (Double) one : Double.parseDouble(one.toString());
					final Double second = (two instanceof Double) ? (Double) two : Double.parseDouble(two.toString());

					return first.equals(second);
				}
				case BOOLEAN:
				{
					final Boolean first = (one instanceof Boolean) ? (Boolean) one : Boolean.parseBoolean(one.toString());
					final Boolean second = (two instanceof Boolean) ? (Boolean) two : Boolean.parseBoolean(two.toString());
					return first.equals(second);
				}
				case BLOB:
				{
					final byte[] first = (one instanceof byte[]) ? (byte[]) one : Base64.getDecoder().decode(one.toString());
					final byte[] second = (two instanceof byte[]) ? (byte[]) two : Base64.getDecoder().decode(two.toString());
					return Arrays.equals(first, second);
				}
				case KCHAR:
				{
					final String first = (one instanceof I18nKey) ? ((I18nKey) one).getKey() : one.toString();
					final String second = (two instanceof I18nKey) ? ((I18nKey) two).getKey() : two.toString();
					return first.equals(second);
				}
				default:
					return false;
			}
		}
		else
		{
			return false;
		}
	}

	private Object getValue(Object value, boolean wrap)
	{
		if(value == null)
		{
			return null;
		}
		switch (this) {
			case SMALLINT -> {
				if (!(value instanceof Short)) {
					return Short.valueOf(value.toString());
				} else {
					return value;
				}
			}
			case INTEGER -> {
				if (!(value instanceof Integer)) {
					return Integer.valueOf(value.toString());
				} else {
					return value;
				}
			}
			case BIGINT -> {
				if (!(value instanceof Long)) {
					return Long.valueOf(value.toString());
				} else {
					return value;
				}
			}
			case DECIMAL -> {
				if (!(value instanceof Double)) {
					return Double.valueOf(value.toString());
				} else {
					return value;
				}
			}
			case KCHAR -> {
				if (!(value instanceof I18nKey)) {
					if (wrap) {
						return new I18nKey(value.toString());
					} else {
						return value.toString();
					}
				} else {
					if (wrap) {
						return value;
					} else {
						return ((I18nKey) value).getKey();
					}
				}
			}
			case BOOLEAN -> {
				if (!(value instanceof Boolean)) {
					return Boolean.valueOf(value.toString());
				} else {
					return value;
				}
			}
			case BLOB -> {
				if (!(value instanceof Byte[])) {
					return Base64.getDecoder().decode(value.toString());
				} else {
					return value;
				}
			}
			case CHAR, SCHAR, TEXT, STEXT -> {
				return value.toString();
			}
			default -> throw new IllegalStateException("Unexpected value: " + value);
		}
	}

	public static DataType get(String type)
	{
		return switch (type) {
			case "CHAR" -> CHAR;
			case "SCHAR" -> SCHAR;
			case "KCHAR" -> KCHAR;
			case "SMALLINT" -> SMALLINT;
			case "INTEGER" -> INTEGER;
			case "BIGINT" -> BIGINT;
			case "DECIMAL" -> DECIMAL;
			case "BOOLEAN" -> BOOLEAN;
			case "TEXT" -> TEXT;
			case "STEXT" -> STEXT;
			case "BLOB" -> BLOB;
			default -> throw ErrorCode.get(ErrorCodes.NOT_SUPPORTED);
		};
	}

	public boolean equals(Object one, Object two)
	{
		if(one == null)
		{
			return two == null;
		}
		else
		{
			if(this == CHAR)
			{
				return one.toString().equalsIgnoreCase(two.toString());
			}
			else
			{
				return one.equals(two);
			}
		}

	}
}
