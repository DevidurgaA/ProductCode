package com.tlc.sql.internal.meta;

import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.api.DataContainer;
import com.tlc.sql.api.ds.AdminDataStore;
import com.tlc.sql.api.meta.PKDefinition;
import com.tlc.sql.api.meta.SequenceDefinition;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.parser.*;
import com.tlc.sql.internal.sequence.BatchSeqGeneratorImpl;
import com.tlc.sql.internal.sequence.SequenceGeneratorImpl;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;


/**
 * @author Abishek
 * @version 1.0
 */
public class MetaDataHandler
{
	private final AdminDataStore adminDataStore;
	public MetaDataHandler(AdminDataStore adminDataStore)
	{
		this.adminDataStore = Objects.requireNonNull(adminDataStore);
	}

	public void loadMetaData(File file)
	{
		final Map<String, TableDefinition> tableDefinitions = fetchTableDefinitions(file);
		loadMetaData(tableDefinitions);
	}

	public void loadMetaData(Map<String, TableDefinition> tableDefinitions)
	{
		MetaCache.get().put(tableDefinitions);
		initSequenceGenerator(tableDefinitions);
	}

	public void populateMetaData(File file)
	{
		final Map<String, TableDefinition> tableDefinitions = fetchTableDefinitions(file);
		populateMetaData(tableDefinitions);
	}

	public void populateMetaData(Map<String, TableDefinition> tableDefinitions)
	{
		adminDataStore.createTables(tableDefinitions.values());
		MetaCache.get().put(tableDefinitions);
		initSequenceGenerator(tableDefinitions);
	}

	public void unloadMetaData(Set<String> tables)
	{
		MetaCache.get().remove(tables);
	}

	public Map<String, TableDefinition> fetchTableDefinitions(File file)
	{
		try(FileInputStream inputStream = new FileInputStream(file))
		{
			return fetchTableDefinitions(inputStream);
		}
		catch(Exception exp)
		{
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_INVALID_FILE, exp);
		}
	}

	public Map<String, TableDefinition> fetchTableDefinitions(InputStream inputStream)
	{
		final Map<String, TableDefinition> definitions = new LinkedHashMap<>();
		final TableDefinitionLoader definitionProvider = new TableDefinitionLoader()
		{
			final MetaCache metaCache = MetaCache.get();
			@Override
			public void add(TableDefinition tableDefinition)
			{
				definitions.put(tableDefinition.getName(), tableDefinition);
			}

			@Override
			public boolean isDuplicate(String tableName)
			{
				return resolve(tableName) != null;
			}

			@Override
			public TableDefinition resolve(String tableName)
			{
				final TableDefinition definition = definitions.get(tableName);
				return definition == null ? metaCache.get(tableName) : definition;
			}
		};
		MetaDataParser.processMetaData(inputStream, definitionProvider);
		return definitions;
	}

	public void populateData(List<File> files, Map<String, String> dynamicProp)
	{
		final PatternResolver patternResolver = new StaticPatternResolver();
		final DataContainer dataContainer = fetchDataContainer(files, patternResolver, dynamicProp, XmlToDCConverter.Type.INITIALIZE);
		adminDataStore.commitChanges(dataContainer);
	}

	public DataContainer fetchDataContainer(List<File> files, PatternResolver patternResolver, Map<String, String> dynamicProp, XmlToDCConverter.Type type)
	{
		final DataContainer dataContainer = DataContainer.create();
		final XmlToDCConverter xmlToDCConverter = XmlToDCConverter.getInstance();
		for (File file : files)
		{
			final DataContainer xmlContainer = xmlToDCConverter.convert(file, patternResolver, dynamicProp::get, type);
			dataContainer.append(xmlContainer);
		}
		return dataContainer;
	}

	private void initSequenceGenerator(Map<String, TableDefinition> tableDefinitions)
	{
		for (TableDefinition tableDefinition : tableDefinitions.values())
		{
			final PKDefinition pkDefinition = tableDefinition.getPkDefinition();
			if(pkDefinition.hasSequenceGenerator())
			{
				final SequenceDefinition sequenceDefinition = pkDefinition.getSequenceDefinition();
				final String sequenceName = sequenceDefinition.getSequenceName();
				if(sequenceDefinition.getIncrementBy() == 1)
				{
					sequenceDefinition.setSequenceGenerator(new SequenceGeneratorImpl(sequenceName, adminDataStore));
				}
				else
				{
					sequenceDefinition.setSequenceGenerator(new BatchSeqGeneratorImpl(sequenceName, adminDataStore));
				}
			}
		}
	}

}
