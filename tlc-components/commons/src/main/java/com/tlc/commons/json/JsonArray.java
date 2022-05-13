package com.tlc.commons.json;


import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;


/**
 * @author Abishek
 * @version 1.0
 */
public interface JsonArray extends Serializable
{
    JsonArray put(Boolean value);

    JsonArray put(Short value);

    JsonArray put(Integer value);

    JsonArray put(Long value);

    JsonArray put(Float value);

    JsonArray put(Double value);

    JsonArray put(Number value);

    JsonArray put(String value);

    JsonArray put(Object value);

    JsonArray put(JsonObject value);

    JsonArray put(JsonArray value);

    JsonArray append(JsonArray json);

    boolean getBoolean(int index);

    boolean optBoolean(int index);

    boolean optBoolean(int index, boolean defaultValue);

    short getShort(int index);

    short optShort(int index, short defaultValue);

    int getInt(int index);

    int optInt(int index, int defaultValue);

    long getLong(int index);

    long optLong(int index, long defaultValue);

    float getFloat(int index);

    float optFloat(int index, float defaultValue);

    double getDouble(int index);

    double optDouble(int index, double defaultValue);

    String getString(int index);

    String optString(int index, String defaultValue);

    JsonArray getJsonArray(int index);

    JsonArray optJsonArray(int index);

    JsonObject getJsonObject(int index);

    JsonObject optJsonObject(int index);

    JsonArray remove(int index);

    int size();

    void forEach(Consumer<Object> consumer);

    String toString();

    byte[] getBytes();

    void writeBytes(OutputStream outputStream);

    List<Object> toList();
}
