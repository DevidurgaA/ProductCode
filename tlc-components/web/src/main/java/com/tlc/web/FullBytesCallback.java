package com.tlc.web;

/**
 * @author Abishek
 * @version 1.0
 */
public interface FullBytesCallback
{
    void handle(WebExchange exchange, byte[] data);
}
