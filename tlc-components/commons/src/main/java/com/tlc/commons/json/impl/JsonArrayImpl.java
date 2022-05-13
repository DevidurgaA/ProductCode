package com.tlc.commons.json.impl;

import com.tlc.commons.json.JsonArray;
import com.tlc.commons.json.JsonObject;
import com.tlc.commons.json.JsonParser;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Abishek
 * @version 1.0
 */
public class JsonArrayImpl extends AbstractJsonArray
{
    private final JsonParser jsonParser;
    public JsonArrayImpl(JsonParser jsonParser)
    {
        this.jsonParser = jsonParser;
    }

    public JsonArrayImpl(JsonParser jsonParser, List<?> data)
    {
        super(data);
        this.jsonParser = jsonParser;
    }

    @Override
    protected JsonArray wrappedList(List<?> collection)
    {
        return new JsonArrayImpl(jsonParser, collection);
    }

    @Override
    protected JsonObject wrappedMap(Map<?, ?> map)
    {
        return new JsonObjectImpl(jsonParser, map);
    }

    @Override
    public String toString()
    {
        return jsonParser.encodeAsString(getList());
    }

    @Override
    public byte[] getBytes()
    {
        return jsonParser.encodeAsBytes(getList());
    }

    @Override
    public void writeBytes(OutputStream outputStream)
    {
        jsonParser.encodeAsBytes(getList(), outputStream);
    }
}
