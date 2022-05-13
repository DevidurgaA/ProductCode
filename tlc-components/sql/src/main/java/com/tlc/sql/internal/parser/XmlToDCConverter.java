package com.tlc.sql.internal.parser;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.Row;
import com.tlc.sql.api.dml.Table;
import com.tlc.sql.api.meta.*;
import com.tlc.sql.internal.status.SQLErrorCodes;
import com.tlc.sql.resource.FGSEQUENCEPATTERN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Abishek
 * @version 1.0
 */
public class XmlToDCConverter
{
	private static final String DYNAMICDATA_KEY = "LOADDYNAMICDATA";
	private static final XmlToDCConverter INSTANCE;
	static
	{
		INSTANCE = new XmlToDCConverter();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlToDCConverter.class);
	public static XmlToDCConverter getInstance()
	{
		return INSTANCE;
	}

	private final Pattern regexPKPattern;
	private XmlToDCConverter()
	{
		this.regexPKPattern = Pattern.compile("[A-Za-z0-9:_]+");
	}

	public enum Type
	{
		INITIALIZE, FETCH, MIXED // Populate, -, Upgrade
	}

	public DataContainer convert(File xmlFile, PatternResolver patternResolver, DynamicDataProvider dyProvider, Type type)
	{
		try
		{
			LOGGER.info( "Processing XmlFile : {}", xmlFile);
			final DataContainer dataContainer = DataContainer.create();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(xmlFile);
			final Element rootElement = doc.getDocumentElement();

			final NodeList nodeList = rootElement.getElementsByTagName(MetaTagConstants.MODULE);
			final int totalNodes = nodeList.getLength();
			LOGGER.info( "Total Modules found : {}", totalNodes);
			for (int index = 0; index < totalNodes; index++)
			{
				final Element element = (Element) nodeList.item(index);
				loadRowsFromElement(element, dataContainer, Collections.emptyMap(), patternResolver, dyProvider, type);
			}
			LOGGER.info( "Successfully processed XmlFile : {}", xmlFile);
			return dataContainer;
		}
		catch(Exception exp)
		{
			LOGGER.info( "Failed to process XmlFile : {}", xmlFile);
			throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_FILE, exp);
		}
	}

	private void loadRowsFromElement(Element ele, DataContainer dc, Map<String, Row> parentRows, PatternResolver patternResolver,
									 DynamicDataProvider dyProvider, Type type)
	{
		if (ele.hasChildNodes())
		{
			final NodeList nodeList = ele.getChildNodes();
			final int totalNodes = nodeList.getLength();
			final Table sequenceTable = Table.get(FGSEQUENCEPATTERN.TABLE);
			for(int index = 0 ; index < totalNodes ; index++)
			{
				final Node childNode =  nodeList.item(index);
				if(childNode instanceof final Element rowElement)
				{
					final String tableName = rowElement.getTagName();
					if (tableName == null)
					{
						throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_TABLE_NAME);
					}

					final Table table = Table.get(tableName);
					final boolean isDynamicData = Boolean.parseBoolean(rowElement.getAttribute(DYNAMICDATA_KEY));

					final ColumnDataHandler dataHandler = new ColumnDataHandler(rowElement, tableName,
							isDynamicData, dyProvider, patternResolver, parentRows);

					final RowInfo rowInfo = constructRowInfo(table, sequenceTable, dataHandler, type);

					final Row dataRow = rowInfo.row;

					final TableDefinition tableDefinition = table.getTableDefinition();
					final Map<String, FKDefinition> fkDefinitionMap = tableDefinition.getFKDefinitions();

					final Map<String, FKDefinition> columnVsFkMap = fkDefinitionMap.values().stream()
								.collect(Collectors.toMap(fk -> fk.getLocal().getColumnName(), fk -> fk));

					final Map<String, ColumnDefinition> otherColumns = tableDefinition.getEditableColumns();
					for (ColumnDefinition columnDefinition : otherColumns.values())
					{
						final String columnName = columnDefinition.getColumnName();
						final Object columnData = getColumnValue(rowInfo.pkPattern, dataHandler, columnDefinition, columnVsFkMap);
						dataRow.set(columnName, columnData);
					}

					if(rowInfo.seqRow != null)
					{
						dc.addNewRow(rowInfo.seqRow);
						dc.addNewRow(dataRow);
					}
					else
					{
						dc.storeRow(dataRow);
					}
					final Map<String, Row> newParentTree = new HashMap<>(parentRows);
					newParentTree.put(tableName, dataRow);
					loadRowsFromElement(rowElement, dc, newParentTree, patternResolver, dyProvider, type);
				}
			}
		}
	}

	private RowInfo constructRowInfo(Table table, Table sequenceTable, ColumnDataHandler dataHandler, Type type)
	{
		final TableDefinition tableDefinition = table.getTableDefinition();
		final String tableName = tableDefinition.getName();

		final PKDefinition pkdefinition = tableDefinition.getPkDefinition();
		final String pkColumnName = pkdefinition.geColumnName();
		final String pkPattern = dataHandler.get(pkColumnName);
		if (pkPattern == null)
		{
			LOGGER.error( "PKColumn not found in tableName {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_PK_VALUE);
		}
		if(!regexPKPattern.matcher(pkPattern).matches())
		{
			LOGGER.error( "Invalid PkColumn pattern mentioned in tableName {}. PKValue {}, Expected {}",
					tableName, pkPattern, regexPKPattern.pattern());
			throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_PK_PATTERN);
		}
		final Long existingPKValue =  dataHandler.getPKValue(pkPattern);
		if(type == Type.INITIALIZE || type == Type.MIXED)
		{
			if(existingPKValue != null)
			{
				if(type == Type.MIXED)
				{
					return new RowInfo(pkPattern, new Row(table, existingPKValue), null);
				}
				else
				{
					LOGGER.error( "Duplicate PkColumn Pattern, tableName {}, PKValue {}", tableName, pkPattern);
					throw ErrorCode.get(SQLErrorCodes.DB_DATA_DUPLICATE_PK_PATTERN);
				}
			}
			else
			{
				final Row row = new Row(table);
				final Long pkValue = row.get(pkColumnName);
				dataHandler.addPkPattern(pkPattern, pkValue);

				final Row sequenceRow = new Row(sequenceTable);
				sequenceRow.set(FGSEQUENCEPATTERN.TABLE_NAME, tableName);
				sequenceRow.set(FGSEQUENCEPATTERN.PATTERN, pkPattern);
				sequenceRow.set(FGSEQUENCEPATTERN.COMMIT_VALUE, pkValue);
				return new RowInfo(pkPattern, row, sequenceRow);
			}
		}
		else
		{
			if(existingPKValue == null)
			{
				LOGGER.error( "PK Pattern Not Found, tableName {}, PKValue {}", tableName, pkPattern);
				throw ErrorCode.get(SQLErrorCodes.DB_DATA_PK_NOT_FOUND);
			}
			return new RowInfo(pkPattern, new Row(table, existingPKValue), null);
		}
	}

	private static Object getColumnValue(String pkPattern, ColumnDataHandler dataHandler, ColumnDefinition columnDef, Map<String, FKDefinition> fkDefinitions)
	{
		final String columnName = columnDef.getColumnName();
		final String columnValue = dataHandler.get(columnName);
		final FKDefinition fkDefinition = fkDefinitions.get(columnName);
		if(fkDefinition != null)
		{
			final String remoteTable = fkDefinition.getReferenceTable();
			if(columnValue == null)
			{
				final String remoteColumn = fkDefinition.getRemote().getColumnName();
				final Row remoteTableRow = dataHandler.getParentRow(remoteTable);
				if(remoteTableRow != null)
				{
					final Object refColumnValue = remoteTableRow.get(remoteColumn);
					if(refColumnValue != null)
					{
						return refColumnValue;
					}
				}
			}
			else
			{
				final Long remoteValue = dataHandler.getFKValue(remoteTable, columnValue);
				if(remoteValue == null)
				{
					LOGGER.error( "Invalid FK column value, please check the Pattern. PKColumn : {}, Column : {}", pkPattern, columnName);
					throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_FK_COLUMN_VALUE);
				}
				return remoteValue;
			}
		}
		else
		{
			if(columnValue == null)
			{
				final Object defaultValue = columnDef.getDefaultValue();
				if(defaultValue != null)
				{
					return defaultValue;
				}
			}
			else
			{
				final DataType dataType = columnDef.getDataType();
				try
				{
					return dataType.getWrappedValue(columnValue);
				}
				catch(Exception exp)
				{
					LOGGER.error( "Column dataType mismatch, PKColumn : {}, Column : {}, Value : {}, dataType : {}", pkPattern, columnName, columnValue, dataType);
					throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_COLUMN_VALUE);
				}
			}
		}

		if(!columnDef.isNullable())
		{
			LOGGER.error( "Column cannot be null  PKColumn : {}, Column : {}", pkPattern, columnName);
			throw ErrorCode.get(SQLErrorCodes.DB_DATA_INVALID_COLUMN_VALUE);
		}
		else
		{
			return null;
		}
	}

	private static class ColumnDataHandler
	{
		private final Element rowElement;
		private final boolean isDynamicData;
		private final DynamicDataProvider dyProvider;

		private final PatternResolver patternResolver;
		private final Map<String, Row> parentRows;
		private final String tableName;
		ColumnDataHandler(Element rowElement, String tableName, boolean isDynamicData,
						  DynamicDataProvider dyProvider, PatternResolver patternResolver, Map<String, Row> parentRows)
		{
			this.rowElement = Objects.requireNonNull(rowElement);
			this.isDynamicData = isDynamicData;
			this.dyProvider = Objects.requireNonNull(dyProvider);
			this.tableName = Objects.requireNonNull(tableName);

			this.patternResolver = Objects.requireNonNull(patternResolver);
			this.parentRows = Objects.requireNonNull(parentRows);
		}

		String get(String attribute)
		{
			final String value = rowElement.getAttribute(attribute);
			if(value.isEmpty())
			{
				return null;
			}
			else if(isDynamicData)
			{
				return dyProvider.get(value);
			}
			else
			{
				return value;
			}
		}

		Long getPKValue(String pattern)
		{
			return patternResolver.getValue(tableName, pattern);
		}

		Long getFKValue(String tableName, String pattern)
		{
			return patternResolver.getValue(tableName, pattern);
		}

		boolean isPatternExists(String pattern)
		{
			return patternResolver.patternExists(tableName, pattern);
		}

		Row getParentRow(String tableName)
		{
			return parentRows.get(tableName);
		}

		void addPkPattern(String pkPattern, Long pkValue)
		{
			patternResolver.addValue(tableName, pkPattern, pkValue);
		}
	}

	private static class RowInfo
	{
		private final String pkPattern;
		private final Row row;
		private final Row seqRow;

		private RowInfo(String pkPattern, Row row, Row seqRow)
		{
			this.pkPattern = pkPattern;
			this.row = row;
			this.seqRow = seqRow;
		}
	}
}
