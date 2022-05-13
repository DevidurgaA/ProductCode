package com.tlc.commons.json;


import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;


/**
 * @author Abishek
 * @version 1.0
 */
public interface JsonObject extends Serializable
{
    boolean containsKey(String key);

    JsonObject put(String key, Boolean value);

    JsonObject put(String key, Short value);

    JsonObject put(String key, Integer value);

    JsonObject put(String key, Long value);

    JsonObject put(String key, Float value);

    JsonObject put(String key, Double value);

    JsonObject put(String key, Number value);

    JsonObject put(String key, String value);

    JsonObject put(String key, JsonArray value);

    JsonObject put(String key, JsonObject value);

    JsonObject put(String key, Object value);

    JsonObject append(JsonObject json);

    boolean getBoolean(String key);

    boolean optBoolean(String key);

    boolean optBoolean(String key, boolean defaultValue);

    short getShort(String key);

    short optShort(String key, short defaultValue);

    int getInt(String key);

    int optInt(String key, int defaultValue);

    long getLong(String key);

    long optLong(String key, long defaultValue);

    float getFloat(String key);

    float optFloat(String key, float defaultValue);

    double getDouble(String key);

    double optDouble(String key, double defaultValue);

    String getString(String key);

    String optString(String key, String defaultValue);

    Object get(String key);

    Object opt(String key);

    JsonArray getJsonArray(String key);

    JsonArray optJsonArray(String key);

    JsonObject getJsonObject(String key);

    JsonObject optJsonObject(String key);

    JsonObject remove(String key);

    int size();

    void forEach(BiConsumer<String, Object> consumer);

    String toString();

    byte[] getBytes();

    void writeBytes(OutputStream outputStream);

    Set<String> keySet();

    Map<String, Object> toMap();
}
