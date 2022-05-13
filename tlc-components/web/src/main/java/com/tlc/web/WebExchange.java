package com.tlc.web;

import java.net.HttpCookie;
import java.util.stream.Stream;

/**
 * @author Abishek
 * @version 1.0
 */
public interface WebExchange
{
    HttpCookie cookie(String name);

    Stream<String> param(String name);

    Stream<String> header(String name);

    void header(String name, String value);

    String ipAddress();

    void redirect(String uri);

    void cookie(HttpCookie cookie);

    RequestReceiver requestReceiver();

    ResponseSender responseSender();
}
