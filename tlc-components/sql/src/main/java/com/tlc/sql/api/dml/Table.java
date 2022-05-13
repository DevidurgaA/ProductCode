package com.tlc.sql.api.dml;

import com.tlc.cache.Cache;
import com.tlc.cache.CacheConfig;
import com.tlc.cache.CacheManager;
import com.tlc.commons.code.ErrorCode;
import com.tlc.sql.internal.meta.MetaCache;
import com.tlc.sql.api.meta.TableDefinition;
import com.tlc.sql.internal.status.SQLErrorCodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author Abishek
 * @version 1.0
 */
public class Table implements Comparable<Table>
{
	private final String tableName;
	private final String alias;
	private final Map<String, Column> column_cache;
	private final int uniqueSeqId;
	private final boolean hasAlias;

	private final Column pkColumn;

	private final TableDefinition tableDefinition;
	
	private static final String FORMAT = "%s_%s";
	private static final Cache<String, Table> TABLE_CACHE;

	private static final MetaCache TABLE_DEFINITIONS;
	static
	{
		final CacheConfig<String, Table> config = new CacheConfig<>();
		config.setExpireAfterAccess(TimeUnit.MINUTES.toMillis(10));
		TABLE_CACHE = CacheManager.getInstance().createCache(config);
		TABLE_DEFINITIONS = MetaCache.get();
	}

	public static Table get(String name)
	{
		return TABLE_CACHE.computeIfAbsent(name, tableName ->  new Table(name, null));
	}

	public Table(String name, String alias)
	{
		this.tableName = Objects.requireNonNull(name).trim();
		this.tableDefinition = TABLE_DEFINITIONS.get(tableName);
		if(tableDefinition == null)
		{
			throw ErrorCode.get(SQLErrorCodes.DB_SCHEMA_UNKNOWN_TABLE, name);
		}
		this.uniqueSeqId = tableDefinition.getSeqId();
		if(alias == null)
		{
			this.hasAlias = true;
			this.alias = Integer.toString(uniqueSeqId);
		}
		else
		{
			this.hasAlias = !name.equals(alias);
			this.alias = Objects.requireNonNull(alias);
		}
		this.column_cache = new HashMap<>();
		final String pkColumnName = tableDefinition.getPkDefinition().geColumnName();
		column_cache.put(pkColumnName, new Column(this, pkColumnName, pkColumnName, true));
		this.pkColumn = getColumn(tableDefinition.getPkDefinition().geColumnName());
	}

	public Table(TableDefinition tableDefinition, String alias)
	{
		this.tableName = tableDefinition.getName();
		this.tableDefinition = tableDefinition;
		this.uniqueSeqId = tableDefinition.getSeqId();
		if(alias == null)
		{
			this.hasAlias = true;
			this.alias = Integer.toString(uniqueSeqId);
		}
		else
		{
			this.hasAlias = !tableName.equals(alias);
			this.alias = Objects.requireNonNull(alias);
		}
		this.column_cache = new HashMap<>();
		final String pkColumnName = tableDefinition.getPkDefinition().geColumnName();
		column_cache.put(pkColumnName, new Column(this, pkColumnName, pkColumnName, true));
		this.pkColumn = getColumn(tableDefinition.getPkDefinition().geColumnName());
	}

	public TableDefinition getTableDefinition()
	{
		return tableDefinition;
	}

	public Column getPKColumn()
	{
		return pkColumn;
	}

	public Column getColumn(String columnName)
	{
		return column_cache.computeIfAbsent(columnName, k-> new Column(this, columnName, columnName, false));
	}
	
	public Column getColumn(String columnName, String columnAlias)
	{
		final String key = String.format(FORMAT, columnName, columnAlias);
		return column_cache.computeIfAbsent(key, k-> new Column(this, columnName, columnAlias, false));
	}

	public String getName()
	{
		return tableName;
	}

	public boolean hasAlias()
	{
		return hasAlias;
	}

	public String getAlias()
	{
		return alias;
	}

	@Override
	public String toString()
	{
		if(tableName.equals(alias))
		{
			return tableName;
		}
		else
		{
			return tableName+"_"+alias;
		}
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof Table)
		{
			return this.hashCode() == object.hashCode();
		}
		return false;
	}
	
	public boolean equals(Table table)
	{
		return this.uniqueSeqId == table.uniqueSeqId;
	}

	public int getSeqId()
	{
		return uniqueSeqId;
	}

	@Override
	public int hashCode()
	{
		return Integer.hashCode(uniqueSeqId);
	}

	@Override
	public int compareTo(Table table)
	{
		return Integer.compare(uniqueSeqId, table.uniqueSeqId);
	}
}
