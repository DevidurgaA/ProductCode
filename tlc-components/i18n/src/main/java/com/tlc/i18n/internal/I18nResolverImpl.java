package com.tlc.i18n.internal;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.logical.And;
import com.googlecode.cqengine.query.logical.Or;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.tlc.cache.Cache;
import com.tlc.cache.CacheManager;
import com.tlc.i18n.I18nKey;
import com.tlc.i18n.I18nResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Abishek
 * @version 1.0
 */
public class I18nResolverImpl implements I18nResolver
{
    private final IndexedCollection<I18nField> indexedData;
    private final Cache<String, String> i18nRepo;
    private final Locale locale;
    private static final Logger LOGGER = LoggerFactory.getLogger(I18nResolver.class);

   private static final Attribute<I18nField, String> GROUP = new SimpleAttribute<>("group")
   {
       public String getValue(I18nField data, QueryOptions queryOptions) { return data.group(); }
   };

   private static final Attribute<I18nField, String> VALUE = new SimpleAttribute<>("value")
   {
       public String getValue(I18nField data, QueryOptions queryOptions) { return data.value(); }
   };

    I18nResolverImpl(Locale locale)
    {
        this.locale = Objects.requireNonNull(locale);
        this.i18nRepo = CacheManager.getInstance().createCache();
        this.indexedData = new ConcurrentIndexedCollection<>();
        
       indexedData.addIndex(HashIndex.onAttribute(GROUP));
       indexedData.addIndex(SuffixTreeIndex.onAttribute(VALUE));
       indexedData.addIndex(RadixTreeIndex.onAttribute(VALUE));
    }

    @Override
    public String get(String key)
    {
        return getInternal(key);
    }

    @Override
    public String get(I18nKey key)
    {
        return getInternal(key.getKey());
    }

    @Override
    public String get(I18nKey key, Object... params)
    {
        return getInternal(key.getKey(), params);
    }

    @Override
    public String get(String key, Object... params)
    {
        return getInternal(key, params);
    }

    @Override
    public Collection<String> getKeysStartsWithValues(String group, Collection<String> values)
    {
        final Set<String> dataSet = new HashSet<>();
        final List<Query<I18nField>> list = new ArrayList<>();
        for(String value : values)
        {
            list.add(QueryFactory.startsWith(VALUE, value.toLowerCase(locale)));
        }
        loadDataSet(dataSet, group, list);
        return dataSet;
    }

    @Override
    public Collection<String> getKeysEqualsValues(String group, Collection<String> values)
    {
        final Set<String> dataSet = new HashSet<>();
        final List<Query<I18nField>> list = new ArrayList<>();
        for(String value : values)
        {
            list.add(QueryFactory.equal(VALUE, value.toLowerCase(locale)));
        }
        loadDataSet(dataSet, group, list);
        return dataSet;
    }

    @Override
    public Collection<String> getKeysContainsValues(String group, Collection<String> values)
    {
        final Set<String> dataSet = new HashSet<>();
        final List<Query<I18nField>> list = new ArrayList<>();
        for(String value : values)
        {
            list.add(QueryFactory.contains(VALUE, value.toLowerCase(locale)));
        }
        loadDataSet(dataSet, group, list);
        return dataSet;
    }

    private String getInternal(String key, Object... params)
    {
        if(key != null && key.startsWith("i18n."))
        {
            final String value = i18nRepo.get(key);
            return value != null ? (params != null ? MessageFormat.format(value.replace("'", "''"), params) : value) : key;
        }
        return key;
    }

    private void loadDataSet(Set<String> dataSet, String group, List<Query<I18nField>> list)
    {
        final Query<I18nField> valuesQuery;
        if(list.size() == 1)
        {
            valuesQuery = list.get(0);
        }
        else
        {
            valuesQuery = new Or<>(list);
        }
        final And<I18nField> query = QueryFactory.and(QueryFactory.equal(GROUP, group), valuesQuery);
        final ResultSet<I18nField> resultSet = indexedData.retrieve(query);
        resultSet.stream().map(I18nField::key).forEach(dataSet::add);
    }
    
    public void load(Map<String, String> keys, Map<String, Set<String>> index)
    {
        LOGGER.info("Loading i18n keys for locale: {}, number of keys : {}", locale.toString(), keys.size());
        i18nRepo.putAll(keys);
        index.forEach( (group, set) ->
        {
            final Map<String, String> discovered = i18nRepo.getAll(set);
            final List<I18nField> fields = new ArrayList<>(discovered.size());
            discovered.forEach( (key, value) -> fields.add(new I18nField(group, key, value.toLowerCase(locale))));
            indexedData.addAll(fields);
        });
    }

    public void unload(Map<String, String> keys, Map<String, Set<String>> index)
    {
        LOGGER.info("Unloading i18n keys for locale: {}, number of keys : {}", locale.toString(), keys.size());
        index.forEach( (group, set) ->
        {
            final Map<String, String> discovered = i18nRepo.getAll(set);
            final Set<I18nField> fields = new HashSet<>(discovered.size());
            discovered.forEach( (key, value) -> fields.add(new I18nField(group, key, value.toLowerCase(locale))));
            indexedData.removeAll(fields);
        });
        i18nRepo.removeAll(keys.keySet());
    }
}
