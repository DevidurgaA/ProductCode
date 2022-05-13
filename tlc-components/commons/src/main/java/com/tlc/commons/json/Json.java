package com.tlc.commons.json;

import com.tlc.commons.json.impl.JsonArrayImpl;
import com.tlc.commons.json.impl.JsonObjectImpl;
import com.tlc.commons.json.impl.JsonParserImpl;

import java.io.InputStream;
import java.util.*;

/**
 * @author Abishek
 * @version 1.0
 */
public class Json
{
    private final static JsonParser JSON_PARSER = new JsonParserImpl();
    public static JsonArray array()
    {
        return new JsonArrayImpl(JSON_PARSER);
    }

    public static JsonArray array(Collection<?> list)
    {
        return new JsonArrayImpl(JSON_PARSER, new ArrayList<>(list));
    }

    public static JsonArray array(String data)
    {
        final List<?> decoded = JSON_PARSER.decodeJsonArray(data);
        return new JsonArrayImpl(JSON_PARSER, decoded);
    }

    public static JsonArray array(byte[] data)
    {
        final List<?> decoded = JSON_PARSER.decodeJsonArray(data);
        return new JsonArrayImpl(JSON_PARSER, decoded);
    }

    public static JsonArray array(InputStream inputStream)
    {
        final List<?> decoded = JSON_PARSER.decodeJsonArray(inputStream);
        return new JsonArrayImpl(JSON_PARSER, decoded);
    }

    public static JsonObject object()
    {
        return new JsonObjectImpl(JSON_PARSER);
    }

    public static JsonObject object(String data)
    {
        final Map<?, ?> decoded = JSON_PARSER.decodeJsonObject(data);
        return new JsonObjectImpl(JSON_PARSER, decoded);
    }

    public static JsonObject object(Map<String, ?> data)
    {
        return new JsonObjectImpl(JSON_PARSER, new HashMap<>(data));
    }

    public static JsonObject object(byte[] data)
    {
        final Map<?, ?> decoded = JSON_PARSER.decodeJsonObject(data);
        return new JsonObjectImpl(JSON_PARSER, decoded);
    }

    public static JsonObject object(InputStream inputStream)
    {
        final Map<?, ?> decoded = JSON_PARSER.decodeJsonObject(inputStream);
        return new JsonObjectImpl(JSON_PARSER, decoded);
    }
}
