package com.tlc.commons.json.impl;

import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * @author Abishek
 * @version 1.0
 */
public abstract class AbstractJsonArray implements JsonArray
{
    private final List<Object> dataList;
    AbstractJsonArray()
    {
        this.dataList = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    AbstractJsonArray(List<?> data)
    {
        this.dataList = (List<Object>) data;
    }

    @Override
    public JsonArray put(Boolean value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Short value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Integer value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Long value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Float value)
    {
        dataList.add(value);
        return this;
    }

    @Override
    public JsonArray put(Double value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Number value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(String value)
    {
        localPut(value);
        return this;
    }

    @Override
    public JsonArray put(Object value)
    {
        if(value instanceof AbstractJsonArray jsonArray)
        {
            localPut(jsonArray.getList());
        }
        else if(value instanceof AbstractJsonObject jsonObject)
        {
            localPut(jsonObject.getMap());
        }
        else
        {
            localPut(value);
        }
        return this;
    }

    @Override
    public JsonArray put(JsonObject value)
    {
        localPut(((AbstractJsonObject) value).getMap());
        return this;
    }

    @Override
    public JsonArray put(JsonArray value)
    {
        localPut(((AbstractJsonArray) value).getList());
        return this;
    }

    @Override
    public JsonArray append(JsonArray value)
    {
        dataList.addAll(((AbstractJsonArray) value).getList());
        return this;
    }

    @Override
    public boolean getBoolean(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Boolean ? ((Boolean) object)
                : Boolean.parseBoolean((String) object);
    }

    @Override
    public boolean optBoolean(int index)
    {
        try
        {
            return getBoolean(index);
        }
        catch (Exception exp)
        {
            return false;
        }
    }

    @Override
    public boolean optBoolean(int index, boolean defaultValue)
    {
        try
        {
            return getBoolean(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public short getShort(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Number ? ((Number) object).shortValue()
                : Short.parseShort((String) object);
    }

    @Override
    public short optShort(int index, short defaultValue)
    {
        try
        {
            return getShort(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public int getInt(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Number ? ((Number) object).intValue()
                : Integer.parseInt((String) object);
    }

    @Override
    public int optInt(int index, int defaultValue)
    {
        try
        {
            return getInt(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public long getLong(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Number ? ((Number) object).longValue()
                : Long.parseLong((String) object);
    }

    @Override
    public long optLong(int index, long defaultValue)
    {
        try
        {
            return getLong(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Number ? ((Number) object).floatValue()
                : Float.parseFloat((String) object);
    }

    @Override
    public float optFloat(int index, float defaultValue)
    {
        try
        {
            return getFloat(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof Number ? ((Number) object).doubleValue()
                : Double.parseDouble((String) object);
    }

    @Override
    public double optDouble(int index, double defaultValue)
    {
        try
        {
            return getDouble(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public String getString(int index)
    {
        final Object object = dataList.get(index);
        return object instanceof String ? ((String) object)
                : object.toString();
    }

    @Override
    public String optString(int index, String defaultValue)
    {
        try
        {
            return getString(index);
        }
        catch (Exception exp)
        {
            return defaultValue;
        }
    }

    @Override
    public JsonArray getJsonArray(int index)
    {
        final Object object = dataList.get(index);
        return wrappedList((List<?>) object);
    }

    @Override
    public JsonArray optJsonArray(int index)
    {
        try
        {
            return getJsonArray(index);
        }
        catch (Exception exp)
        {
            return null;
        }
    }

    @Override
    public JsonObject getJsonObject(int index)
    {
        final Object object = dataList.get(index);
        return wrappedMap((Map<?, ?>) object);
    }

    @Override
    public JsonObject optJsonObject(int index)
    {
        try
        {
            return getJsonObject(index);
        }
        catch (Exception exp)
        {
            return null;
        }
    }

    @Override
    public JsonArray remove(int index)
    {
        dataList.remove(index);
        return this;
    }

    @Override
    public int size()
    {
        return dataList.size();
    }

    @Override
    public void forEach(Consumer<Object> consumer)
    {
        dataList.forEach(consumer);
    }

    @Override
    public List<Object> toList()
    {
        return List.copyOf(dataList);
    }

    protected List<Object> getList()
    {
        return dataList;
    }

    private void localPut(Object value)
    {
        if(value != null)
        {
            dataList.add(value);
        }
    }

    protected abstract JsonArray wrappedList(List<?> collection);

    protected abstract JsonObject wrappedMap(Map<?, ?> map);
}
