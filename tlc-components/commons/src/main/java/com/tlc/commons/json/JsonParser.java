package com.tlc.commons.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Abishek
 * @version 1.0
 */
public interface JsonParser
{
    Map<String, ?> decodeJsonObject(byte[] data);

    List<?> decodeJsonArray(byte[] data);

    Map<String, ?> decodeJsonObject(InputStream inputStream);

    List<?> decodeJsonArray(InputStream inputStream);

    Map<String, ?> decodeJsonObject(String data);

    List<?> decodeJsonArray(String data);

    String encodeAsString(Map<?, ?> object);

    String encodeAsString(Collection<?> list);

    byte[] encodeAsBytes(Map<?, ?> object);

    byte[] encodeAsBytes(Collection<?> list);

    void encodeAsBytes(Map<?, ?> object, OutputStream outputStream);

    void encodeAsBytes(Collection<?> list, OutputStream outputStream);
}
