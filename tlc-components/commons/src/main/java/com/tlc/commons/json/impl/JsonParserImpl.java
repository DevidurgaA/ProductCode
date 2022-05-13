package com.tlc.commons.json.impl;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.commons.json.JsonParser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public class JsonParserImpl implements JsonParser
{
    private final ObjectMapper mapper;
    private final DslJson<Object> dslJson;

    public JsonParserImpl()
    {
        this.dslJson = new DslJson<>();
        this.mapper = new ObjectMapper();
    }

    /*
         DSL
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ?> decodeJsonObject(byte[] data)
    {
        try
        {
            return dslJson.deserialize(Map.class, data, data.length);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public List<?> decodeJsonArray(byte[] data)
    {
        try
        {
            return dslJson.deserialize(List.class, data, data.length);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ?> decodeJsonObject(InputStream inputStream)
    {
        try
        {
            return dslJson.deserialize(Map.class, inputStream);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public List<?> decodeJsonArray(InputStream inputStream)
    {
        try
        {
            return dslJson.deserialize(List.class, inputStream);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }

    @Override
    public byte[] encodeAsBytes(Map<?, ?> jsonObject)
    {
        try
        {
            final JsonWriter writer = dslJson.newWriter();
            writer.serializeObject(jsonObject);
            return writer.toByteArray();
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public byte[] encodeAsBytes(Collection<?> jsonArray)
    {
        try
        {
            final JsonWriter writer = dslJson.newWriter();
            writer.serializeObject(jsonArray);
            return writer.toByteArray();
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }

    @Override
    public void encodeAsBytes(Map<?, ?> object, OutputStream outputStream)
    {
        try
        {
            final JsonWriter writer = dslJson.newWriter();
            writer.serializeObject(object);
            writer.toStream(outputStream);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public void encodeAsBytes(Collection<?> list, OutputStream outputStream)
    {
        try
        {
            final JsonWriter writer = dslJson.newWriter();
            writer.serializeObject(list);
            writer.toStream(outputStream);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }


    /*
        Jackson
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ?> decodeJsonObject(String data)
    {
        try
        {
            return mapper.readValue(data, Map.class);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public List<?> decodeJsonArray(String data)
    {
        try
        {
            return mapper.readValue(data, List.class);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }

    @Override
    public String encodeAsString(Map<?, ?> jsonObject)
    {
        try
        {
            return mapper.writeValueAsString(jsonObject);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonObject", exp);
        }
    }

    @Override
    public String encodeAsString(Collection<?> jsonArray)
    {
        try
        {
            return mapper.writeValueAsString(jsonArray);
        }
        catch (Exception exp)
        {
            throw ErrorCode.get(ErrorCodes.INVALID_DATA, "Invalid JsonArray", exp);
        }
    }
}
