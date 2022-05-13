package com.tlc.sql.api.meta;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class ColumnDefinition {

	public static final int DEFAULT_COLUMN_LENGTH = 250;
	public static final int DEFAULT_PRECISION = 4;
	public static final int DEFAULT_SCALE = 1;

	private final String columnName;
	private final DataType dataType;
	private final int maxLength;
	private final int precision;
	private final int scale;
	private final Object defaultValue;
	private final boolean nullable;

	public ColumnDefinition(final String columnName, final DataType dataType) {
		this(columnName, dataType, DEFAULT_COLUMN_LENGTH, null, false);
	}

	public ColumnDefinition(final String columnName, final DataType dataType, final boolean nullable) {
		this(columnName, dataType, DEFAULT_COLUMN_LENGTH, null, nullable);
	}

	public ColumnDefinition(final String columnName, final DataType dataType, final Object defaultValue,
							final boolean nullable) {
		this(columnName, dataType, DEFAULT_COLUMN_LENGTH, defaultValue, nullable);
	}

	public ColumnDefinition(final String columnName, final DataType dataType, final int maxLength,
							final Object defaultValue, final boolean nullable) {
		this(columnName, dataType, maxLength, DEFAULT_PRECISION, DEFAULT_SCALE, defaultValue, nullable);
	}

	public ColumnDefinition(final String columnName, final DataType dataType, final int maxLength, final int precision,
							final Object defaultValue, final boolean nullable) {
		this(columnName, dataType, maxLength, precision, DEFAULT_SCALE, defaultValue, nullable);
	}

	public ColumnDefinition(final String columnName, final DataType dataType, final int maxLength, final int precision,
							final int scale, final Object defaultValue, final boolean nullable) {
		if (maxLength < 0) {
			throw ErrorCode.get(SQLErrorCodes.DB_INVALID_COLUMN_MAX_LENGTH);
		}
		this.columnName = Objects.requireNonNull(columnName);
		this.dataType = Objects.requireNonNull(dataType);
		this.maxLength = maxLength;
		this.defaultValue = defaultValue;
		this.nullable = nullable;

		if (DataType.DECIMAL == dataType) {
			if (precision < 1) {
				throw ErrorCode.get(SQLErrorCodes.DB_INVALID_NUMERIC_PRECISION);
			}

			if (scale < 1) {
				throw ErrorCode.get(SQLErrorCodes.DB_INVALID_NUMERIC_SCALE);
			}
			this.precision = precision;
			this.scale = scale;
		} else {
			this.precision = 0;
			this.scale = 0;
		}
	}

	public ColumnDefinition copyNullable() {
		return new ColumnDefinition(columnName, dataType, maxLength, precision, scale, defaultValue, true);
	}

	public DataType getDataType() {
		return dataType;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean isNullable() {
		return nullable;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getPrecision() {
		return precision;
	}

	public int getScale() {
		return scale;
	}
}
