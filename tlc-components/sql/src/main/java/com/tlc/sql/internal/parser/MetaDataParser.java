package com.tlc.sql.internal.parser;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.meta.*;
import com.tlc.sql.internal.status.SQLErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/**
 * @author Abishek
 * @version 1.0
 */
public class MetaDataParser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataParser.class);

	private static final AtomicInteger TABLE_ORDER = new AtomicInteger(100);

	private static final String NAME_FORMAT = "[A-Za-z0-9_]";
	private static final Pattern GLOBAL_PATTERN = Pattern.compile("^"+ NAME_FORMAT +"+$");

	public static void processMetaData(InputStream inputStream, TableDefinitionLoader definitionProvider)
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(inputStream);

			final Element rootElement = doc.getDocumentElement();

			final NodeList modules =  rootElement.getElementsByTagName(MetaTagConstants.MODULE);
			final int totalModules = modules.getLength();
			LOGGER.info("Total Modules found : {}", totalModules);

			for (int index = 0; index < totalModules; index++)
			{
				final Element moduleElement = (Element) modules.item(index);
				final String moduleName = getAttribute(moduleElement, MetaTagConstants.NAME);

				final NodeList allTables =  moduleElement.getElementsByTagName(MetaTagConstants.TABLE);
				final int totalTables = allTables.getLength();

				LOGGER.info("Total tables found : {}, Module : {}", totalTables, moduleName);
				for(int iIndex = 0; iIndex < totalTables; iIndex++)
				{
					final Element element = (Element) allTables.item(iIndex);
					final String name = getAttribute(element, MetaTagConstants.NAME);
					if(name == null)
					{
						LOGGER.error( "Table tag found without name or type");
						throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_TABLE_NAME, "Empty tableName in module : "+moduleName);
					}
					try
					{
						loadTableDefinition(name, element, TABLE_ORDER.getAndIncrement(), definitionProvider);
					}
					catch (Exception exception)
					{
						throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE, "Unable to parse table : "+name, exception);
					}
				}
			}
			LOGGER.info( "Successfully parsed XmlFile");
		}
		catch(Exception exp)
		{
			LOGGER.info( "Failed to process XmlFile ");
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE, exp);
		}
	}
	
	private static void loadTableDefinition(String tableName, Element tableElement, int tableOrder, TableDefinitionLoader definitionProvider)
	{
		final String tableType = getAttribute(tableElement, MetaTagConstants.TYPE);
		final TableType typeEnum = tableType != null ? TableType.get(Integer.parseInt(tableType)) : TableType.COMMON;
		if(!GLOBAL_PATTERN.matcher(tableName).matches())
		{
			LOGGER.error( "Invalid tableName exists, Expected format [A-Za-z0-9_], current {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_TABLE_NAME);
		}
		if(definitionProvider.isDuplicate(tableName))
		{
			LOGGER.error( "Table with same name already exists, table name : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_TABLE_NAME);
		}
		final NodeList columns = tableElement.getElementsByTagName(MetaTagConstants.COLUMNS);
		if(columns.getLength() == 0)
		{
			LOGGER.error( "Columns tag notFound in table : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_LIST);
		}
		final NodeList primaryKey = tableElement.getElementsByTagName(MetaTagConstants.PRIMARY_KEY);
		if(primaryKey.getLength() == 0)
		{
			LOGGER.error( "Primary key tag notFound in table : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_PK_NAME);
		}
		final Pattern name_pattern = Pattern.compile("^"+tableName+"_"+NAME_FORMAT+"+$");

		final TableMetaInput metaInput = new TableMetaInput(tableName);
		loadColumnDefinition(tableName, columns, metaInput);

		if(typeEnum.isOrgDependent() && metaInput.getColumnDefinition(ImmutableColumns.ORG_ID.getName()) == null)
		{
			LOGGER.error( "OrgId column missing in table : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_ORG_COLUMN_MISSING);
		}

		final Set<String> nameContainer = new HashSet<>();
		loadPrimaryKeyDefinition(primaryKey, nameContainer, metaInput, name_pattern);

		if(definitionProvider.loadKeys())
		{
			final NodeList foreignKeys = tableElement.getElementsByTagName(MetaTagConstants.FOREIGN_KEYS);
			if(foreignKeys.getLength() > 0)
			{
				loadForeignKeyDefinitions(foreignKeys, metaInput, definitionProvider, nameContainer, name_pattern);
			}

			final NodeList uniqueKeys = tableElement.getElementsByTagName(MetaTagConstants.UNIQUE_KEYS);
			if(uniqueKeys.getLength() > 0)
			{
				loadUniqueKeyDefinitions(uniqueKeys, metaInput, nameContainer, name_pattern);
			}

			final NodeList indexes = tableElement.getElementsByTagName(MetaTagConstants.INDEXES);
			if(indexes.getLength() > 0)
			{
				loadIndexDefinitions(indexes, metaInput, nameContainer, name_pattern);
			}
		}
		definitionProvider.add(new TableDefinition(tableName, typeEnum, tableOrder, metaInput));
	}

	private static void loadColumnDefinition(String tableName, NodeList columns, TableMetaInput metaInput)
	{
		if(columns.getLength() > 1)
		{
			LOGGER.error( "Multiple Columns tag found inside table : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_LIST);
		}
		final Element columnsElement = (Element) columns.item(0);
		final NodeList allColumns = columnsElement.getElementsByTagName(MetaTagConstants.COLUMN);
		if(allColumns.getLength() == 0)
		{
			LOGGER.error( "No valid Column tag found inside table : {}", tableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_LIST);
		}
		for(int index = 0; index < allColumns.getLength(); index++)
		{
			final Element columnElement = (Element) allColumns.item(index);
			final String columnName = getAttribute(columnElement, MetaTagConstants.NAME);
			final String dataTypeStr = getAttribute(columnElement, MetaTagConstants.DATATYPE);

			if(columnName == null || dataTypeStr == null)
			{
				LOGGER.error( "ColumnName and dataType cannot be empty, TableName : {}", tableName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_NAME);
			}
			if(!GLOBAL_PATTERN.matcher(columnName).matches())
			{
				LOGGER.error( "Invalid columnName exists, Expected format [A-Za-z0-9_], TableName : {}  Column : {}", tableName, columnName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_NAME);
			}
			if(metaInput.getColumnDefinition(columnName) != null)
			{
				LOGGER.error( "Already Column with the same name exists, TableName : {}  Column : {}", tableName, columnName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_COLUMN_NAME);
			}
			final DataType dataType = DataType.get(dataTypeStr);
			if(dataType == null)
			{
				LOGGER.error( "Unsupported dataType provided, TableName : {}  Column : {} DataType : {}", tableName, columnName, dataTypeStr);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_DATA_TYPE);
			}

			final int convertedMaxLength;
			final String maxLength = getAttribute(columnElement, MetaTagConstants.MAXLENGTH);
			if(maxLength != null)
			{
				convertedMaxLength = Integer.parseInt(maxLength);
			}
			else
			{
				convertedMaxLength = ColumnDefinition.DEFAULT_COLUMN_LENGTH;
			}

			final boolean isNullable = Boolean.parseBoolean(getAttribute(columnElement, MetaTagConstants.NULLABLE));

			final String defaultValue = getAttribute(columnElement, MetaTagConstants.DEFAULTVALUE);
			final Object convertedDefaultValue;
			if(defaultValue != null && !defaultValue.equalsIgnoreCase("NULL"))
			{
				convertedDefaultValue = dataType.getUnWrappedValue(defaultValue);
			}
			else
			{
				convertedDefaultValue = null;
			}

			if (dataType == DataType.DECIMAL) {
				final String precision = getAttribute(columnElement, MetaTagConstants.PRECISION);
				final String scale = getAttribute(columnElement, MetaTagConstants.SCALE);
				final int convertedPrecision = precision != null ? Integer.parseInt(precision)
						: ColumnDefinition.DEFAULT_PRECISION;
				final int convertedScale = scale != null ? Integer.parseInt(scale)
						: ColumnDefinition.DEFAULT_SCALE;

				metaInput.addColumnDefinition(new ColumnDefinition(columnName, dataType, convertedMaxLength,
						convertedPrecision, convertedScale, convertedDefaultValue, isNullable));
			} else {
				metaInput.addColumnDefinition(new ColumnDefinition(columnName, dataType, convertedMaxLength,
						convertedDefaultValue, isNullable));
			}
		}
	}

	private static void loadForeignKeyDefinitions(NodeList foreignKeysTag, TableMetaInput metaInput, TableDefinitionLoader definitionProvider, Set<String> nameContainer, Pattern name_pattern)
	{
		final String localTableName = metaInput.getTableName();
		if(foreignKeysTag.getLength() > 1)
		{
			LOGGER.error( "Multiple foreign-keys tag found inside table : {}", localTableName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FK_LIST);
		}
		final Element foreignKeysElement = (Element) foreignKeysTag.item(0);
		final NodeList foreignKeys = foreignKeysElement.getElementsByTagName(MetaTagConstants.FOREIGN_KEY);

		for(int index = 0; index < foreignKeys.getLength() ; index++)
		{
			final Element fkElement = (Element) foreignKeys.item(index);
			final String fkName = getAttribute(fkElement, MetaTagConstants.NAME);
			if(fkName == null)
			{
				LOGGER.error( "ForeignKey name not found inside table : {}", localTableName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FK_NAME);
			}
			if(!name_pattern.matcher(fkName).matches())
			{
				LOGGER.error( "Invalid FKName exists, Expected format [A-Za-z0-9_], current {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FK_NAME);
			}
			if(!nameContainer.add(fkName))
			{
				LOGGER.error( "Already Key with the same name exists, Name : {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_KEY_NAME);
			}
			final String referencedTable = getAttribute(fkElement, MetaTagConstants.FK_REFERENCETABLE);
			if(referencedTable == null)
			{
				LOGGER.error( "Invalid table name in FKName {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_TABLE);
			}
			final TableDefinition refTableDef = definitionProvider.resolve(referencedTable);
			if(refTableDef == null)
			{
				LOGGER.error( "Unable to load FK {}, No such table exists {}", fkName, referencedTable);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_TABLE);
			}
			final String localColumn = getAttribute(fkElement, MetaTagConstants.FK_LOCALCOLUMN);
			final String refColumnColumn = getAttribute(fkElement, MetaTagConstants.FK_REFERENCECOLUMN);
			if(localColumn == null || refColumnColumn == null)
			{
				LOGGER.error( "Invalid column name found in fk-column, FK name : {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN_NAME);
			}
			final ColumnDefinition refColumnDef = refTableDef.getColumnDefinition(refColumnColumn);
			final ColumnDefinition localColumnDef = metaInput.getColumnDefinition(localColumn);
			if(refColumnDef == null || localColumnDef == null)
			{
				LOGGER.error( "Unknown column name found in fk-column, FK name : {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_COLUMN);
			}
			final DataType localDataType = localColumnDef.getDataType();
			if(localDataType.isStorageType())
			{
				LOGGER.error( "Column cannot be configured for ForeignKey : {}", localColumn);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
			}
			if(refColumnDef.getDataType() != localDataType)
			{
				LOGGER.error( "Remote and Local column type mismatch inside fk-column, FK name : {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_FK_COLUMN_TYPE_MISMATCH);
			}
			final FKDefinition.FKConstraint fkConstraint = FKDefinition.FKConstraint.get(getAttribute(fkElement, MetaTagConstants.FK_CONSTRAINT));
			if(fkConstraint == null)
			{
				LOGGER.error( "Invalid FK Constraints found, FK name : {}", fkName);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_FK_INVALID_CONSTRAINT);
			}
			final FKDefinition fkDefinition = new FKDefinition(fkName, localTableName, refTableDef, localColumnDef, refColumnDef, fkConstraint);
			loadIndexFromFKDefinition(metaInput, fkDefinition, nameContainer);
			metaInput.addFKDefinition(fkDefinition);
		}
	}

	private static void loadIndexFromFKDefinition(TableMetaInput metaInput, FKDefinition fkDefinition, Set<String> nameContainer)
	{
		final String autoIndexDefName = fkDefinition.getName() + "_auto_index";
		if(!nameContainer.add(autoIndexDefName))
		{
			LOGGER.error( "Already Key with the same Index name(auto) exists, Name : {}", autoIndexDefName);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_KEY_NAME);
		}
		metaInput.addIndexDefinition(new IndexDefinition(autoIndexDefName, List.of(fkDefinition.getLocal().getColumnName())));
	}

	private static void loadUniqueKeyDefinitions(NodeList uniqueKeys, TableMetaInput metaInput, Set<String> nameContainer, Pattern name_pattern)
	{
		if(uniqueKeys.getLength() > 1)
		{
			LOGGER.error( "Multiple unique-keys tag found inside table : {}", metaInput.getTableName());
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_UK_LIST);
		}
		final String pkColumn = metaInput.getPkDefinition().geColumnName();
		final Element uniqueKeysElement = (Element) uniqueKeys.item(0);
		final NodeList uniqueKeyNodes = uniqueKeysElement.getElementsByTagName(MetaTagConstants.UNIQUE_KEY);

		final int length = uniqueKeyNodes.getLength();
		final Set<String> existing_columns = metaInput.getColumns();
		for(int index = 0; index < length; index++)
		{
			final Element uniqueKeyElement = (Element) uniqueKeyNodes.item(index);
			final String name = getAttribute(uniqueKeyElement, MetaTagConstants.NAME);
			if(name == null)
			{
				LOGGER.error( "UniqueKey name not found inside table : {}", metaInput.getTableName());
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_UK_NAME);
			}
			if(!name_pattern.matcher(name).matches())
			{
				LOGGER.error( "Invalid UKName, Expected format [A-Za-z0-9_], current {}", name);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_UK_NAME);
			}
			if(!nameContainer.add(name))
			{
				LOGGER.error( "Already Key with the same name exists, Name : {}", name);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_KEY_NAME);
			}

			final Set<String> ukColumns = new HashSet<>();
			final NodeList uniqueKeyNode = uniqueKeyElement.getElementsByTagName(MetaTagConstants.UNIQUE_KEY_COLUMN);
			for(int innerIndex = 0; innerIndex < uniqueKeyNode.getLength(); innerIndex++)
			{
				final Element element = (Element) uniqueKeyNode.item(innerIndex);
				final String nodeValue = element.getTextContent();
				if(nodeValue == null || nodeValue.isEmpty() || !existing_columns.contains(nodeValue))
				{
					LOGGER.error( "Unknown column found inside UniqueKey : {}", name);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_COLUMN);
				}
				if(nodeValue.equals(pkColumn))
				{
					LOGGER.error( "PK column found inside UniqueKey : {}", name);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
				}
				final ColumnDefinition columnDefinition = metaInput.getColumnDefinition(nodeValue);
				if(columnDefinition.getDataType().isStorageType())
				{
					LOGGER.error( "Column cannot be configured for UniqueKey : {}", nodeValue);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
				}
				ukColumns.add(nodeValue);
			}
			if(ukColumns.isEmpty())
			{
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_UK_CONFIG, name);
			}
			metaInput.addUKDefinition(new UKDefinition(name, ukColumns));
		}
	}

	private static void loadIndexDefinitions(NodeList indexes, TableMetaInput metaInput, Set<String> nameContainer, Pattern name_pattern)
	{
		if(indexes.getLength() > 1)
		{
			LOGGER.error( "Multiple indexes tag found inside table : {}", metaInput.getTableName());
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_INDEX_LIST);
		}
		final Set<String> notAllowedColumns = new HashSet<>();
		notAllowedColumns.add(metaInput.getPkDefinition().geColumnName());
		metaInput.getFkDefinitions().values().stream().map(FKDefinition::getLocal).map(ColumnDefinition::getColumnName).forEach(notAllowedColumns::add);

		final Element indexesElement = (Element) indexes.item(0);
		final NodeList indexNodes = indexesElement.getElementsByTagName(MetaTagConstants.INDEX);
		final Set<String> existing_columns = metaInput.getColumns();
		final int length = indexNodes.getLength();
		for(int index = 0; index < length; index++)
		{
			final Element indexElement = (Element) indexNodes.item(index);
			final String name = getAttribute(indexElement, MetaTagConstants.NAME);
			if(name == null)
			{
				LOGGER.error( "Index name not found inside table : {}", metaInput.getTableName());
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_INDEX_NAME);
			}
			if(!name_pattern.matcher(name).matches())
			{
				LOGGER.error( "Invalid IndexName exists, Expected format [A-Za-z0-9_], current {}", name);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_INDEX_NAME);
			}
			if(!nameContainer.add(name))
			{
				LOGGER.error( "Already Key with the same name exists, Name : {}", name);
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_KEY_NAME);
			}
			final List<String> indexColumns = new ArrayList<>();
			final NodeList uniqueKeyNode = indexElement.getElementsByTagName(MetaTagConstants.INDEX_COLUMN);
			for(int innerIndex = 0; innerIndex < uniqueKeyNode.getLength(); innerIndex++)
			{
				final Element element = (Element) uniqueKeyNode.item(innerIndex);
				final String nodeValue = element.getTextContent();
				if(nodeValue == null || nodeValue.isEmpty() || !existing_columns.contains(nodeValue))
				{
					LOGGER.error( "Unknown column found inside Index : {}", name);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_COLUMN);
				}
				if(notAllowedColumns.contains(nodeValue))
				{
					LOGGER.error( "Invalid column found inside Index : {}", nodeValue);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
				}
				final ColumnDefinition columnDefinition = metaInput.getColumnDefinition(nodeValue);
				if(columnDefinition.getDataType().isStorageType())
				{
					LOGGER.error( "Column cannot be configured for index : {}", nodeValue);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
				}
				if(!indexColumns.contains(nodeValue))
				{
					indexColumns.add(nodeValue);
				}
				else
				{
					LOGGER.error( "Column cannot be configured for index : {}", nodeValue);
					throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_COLUMN_NAME);
				}
			}
			if(indexColumns.isEmpty())
			{
				throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_IDX_CONFIG, name);
			}
			metaInput.addIndexDefinition(new IndexDefinition(name, indexColumns));
		}
	}


	private static void loadPrimaryKeyDefinition(NodeList primaryKeyNodes, Set<String> nameContainer, TableMetaInput metaInput, Pattern name_pattern)
	{
		if(primaryKeyNodes.getLength() > 1)
		{
			LOGGER.error( "Multiple primary-key tag found inside table : {}", metaInput.getTableName());
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_PK_LIST);
		}
		final Element primaryKey = (Element) primaryKeyNodes.item(0);
		
		final String name = getAttribute(primaryKey, MetaTagConstants.NAME);
		final String column = getAttribute(primaryKey, MetaTagConstants.COLUMN);
		final String seqBatch = getAttribute(primaryKey, MetaTagConstants.SEQUENCE_BATCH);
		final String seqGenerator = getAttribute(primaryKey, MetaTagConstants.SEQUENCE_GENERATOR);

		if(name == null || column == null)
		{
			LOGGER.error( "name and column cannot be null in Primary key, reffer table {}", metaInput.getTableName());
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_COLUMN);
		}
		if(!name_pattern.matcher(name).matches())
		{
			LOGGER.error( "Invalid PKName exists,Expected format [A-Za-z0-9_], current {}", name);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_PK_NAME);
		}
		if(!nameContainer.add(name))
		{
			LOGGER.error( "Already Key with the same name exists, Name : {}", name);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_DUPLICATE_KEY_NAME);
		}

		final ColumnDefinition columnDef = metaInput.getColumnDefinition(column);
		if(columnDef == null)
		{
			LOGGER.error( "Unknown column found inside PrimaryKey : {}", column);
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_COLUMN);
		}
		final DataType colDataType = columnDef.getDataType();
		
		if(colDataType != DataType.BIGINT)
		{
			LOGGER.error( "Unsupported PK datatype provided, current datatype : {}", colDataType.getDataTypeStr());
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_PK_COLUMN);
		}
		final int sequenceBatch = seqBatch == null ? 50 : Integer.parseInt(seqBatch);
		if(sequenceBatch < 50)
		{
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_SEQUENCE_CONFIG, "value is < 50");
		}
		if(seqGenerator == null)
		{
			metaInput.setPkDefinition(new PKDefinition(name, column));
		}
		else
		{
			metaInput.setPkDefinition(new PKDefinition(name, column, new SequenceDefinition(seqGenerator, sequenceBatch)));
		}
	}

	private static String getAttribute(Element element, String name)
	{
		final String value = element.getAttribute(name);
		return !value.isEmpty() ? value : null;
	}
}
