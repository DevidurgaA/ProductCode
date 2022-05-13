package com.tlc.web;

/**
 * @author Abishek
 * @version 1.0
 */
public interface PartialBytesCallback
{
    void handle(WebExchange exchange, byte[] message, boolean last);
}
