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
public class JsonObjectImpl extends AbstractJsonObject
{
    private final JsonParser jsonParser;
    public JsonObjectImpl(JsonParser jsonParser)
    {
        this.jsonParser = jsonParser;
    }

    public JsonObjectImpl(JsonParser jsonParser, Map<?, ?> map)
    {
        super(map);
        this.jsonParser = jsonParser;
    }

    @Override
    public String toString()
    {
        return jsonParser.encodeAsString(getMap());
    }

    @Override
    public byte[] getBytes()
    {
        return jsonParser.encodeAsBytes(getMap());
    }

    @Override
    protected JsonArray wrappedList(List<?> collection)
    {
        return new JsonArrayImpl(jsonParser, collection);
    }

    @Override
    public void writeBytes(OutputStream outputStream)
    {
        jsonParser.encodeAsBytes(getMap(), outputStream);
    }

    @Override
    protected JsonObject wrappedMap(Map<?, ?> map)
    {
        return new JsonObjectImpl(jsonParser, map);
    }
}
