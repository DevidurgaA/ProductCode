package com.tlc.i18n.internal;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class I18nField
{
    private final String key;

    private final String value;

    private final String group;

    private Integer hashCode = null;

    static final Attribute<I18nField, String> GROUP = new SimpleAttribute<>("group")
    {
        public String getValue(I18nField data, QueryOptions queryOptions) { return data.group(); }
    };

    static final Attribute<I18nField, String> KEY = new SimpleAttribute<>("key")
    {
        public String getValue(I18nField data, QueryOptions queryOptions) { return data.key(); }
    };

    static final Attribute<I18nField, String> VALUE = new SimpleAttribute<>("value")
    {
        public String getValue(I18nField data, QueryOptions queryOptions) { return data.value(); }
    };

    public I18nField(String group, String key, String value)
    {
        this.group = Objects.requireNonNull(group);
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public String group()
    {
        return group;
    }

    public String key()
    {
        return key;
    }

    public String value()
    {
        return value;
    }

    @Override
    public boolean equals(Object data)
    {
        if(data instanceof I18nField)
        {
            return this.hashCode == data.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if(hashCode == null)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + group.hashCode();
            result = prime * result + key.hashCode();
            this.hashCode = result;
        }
        return hashCode;
    }

}
