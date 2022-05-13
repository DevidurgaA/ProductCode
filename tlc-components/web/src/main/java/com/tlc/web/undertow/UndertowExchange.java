package com.tlc.web.undertow;

import com.tlc.web.*;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Abishek
 * @version 1.0
 */
class UndertowExchange implements WebExchange
{
    private final HttpServerExchange exchange;
    private final UndertowReceiver receiver;
    private final UndertowSender sender;

    UndertowExchange(HttpServerExchange exchange)
    {
        this.exchange = Objects.requireNonNull(exchange);
        this.receiver = new UndertowReceiver();
        this.sender = new UndertowSender();
    }

    @Override
    public HttpCookie cookie(String name)
    {
        final Cookie cookie = exchange.getRequestCookie(name);
        if(cookie != null)
        {
            final HttpCookie rCookie = new HttpCookie(name, cookie.getValue());
            rCookie.setDomain(cookie.getDomain());
            rCookie.setHttpOnly(cookie.isHttpOnly());
            rCookie.setPath(cookie.getPath());
            rCookie.setVersion(cookie.getVersion());
            rCookie.setMaxAge(cookie.getMaxAge());
            return rCookie;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void cookie(HttpCookie cookie)
    {
        final Cookie uCookie = new CookieImpl(cookie.getName(), cookie.getValue());
        if(cookie.getPath() != null)
        {
            uCookie.setPath(cookie.getPath());
        }
        if(cookie.getDomain() != null)
        {
            uCookie.setDomain(cookie.getDomain());
        }
        if(cookie.getMaxAge() != -1)
        {
            uCookie.setMaxAge((int) cookie.getMaxAge());
        }
        if(cookie.getComment() != null)
        {
            uCookie.setComment(cookie.getComment());
        }
        uCookie.setSecure(cookie.getSecure());
        uCookie.setHttpOnly(cookie.isHttpOnly());
        uCookie.setVersion(cookie.getVersion());
        exchange.setResponseCookie(uCookie);
    }

    @Override
    public RequestReceiver requestReceiver()
    {
        return receiver;
    }

    @Override
    public ResponseSender responseSender()
    {
        return sender;
    }

    @Override
    public void redirect(String uri)
    {
        exchange.setStatusCode(StatusCodes.PERMANENT_REDIRECT);
        exchange.getResponseHeaders().put(Headers.LOCATION, uri);
    }

    @Override
    public Stream<String> param(String name)
    {
        final Map<String, Deque<String>> paramsMap = exchange.getQueryParameters();
        final Deque<String> value = paramsMap.get(name);
        return value != null ? value.stream() : null;
    }

    @Override
    public Stream<String> header(String name)
    {
        final HeaderValues headerValues = exchange.getRequestHeaders().get(name);
        return headerValues != null ? headerValues.stream() : null;
    }

    @Override
    public void header(String name, String value)
    {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
    }

    @Override
    public String ipAddress()
    {
        return exchange.getSourceAddress().getAddress().getHostAddress();
    }

    private class UndertowReceiver implements RequestReceiver
    {
        @Override
        public byte[] getBody() throws IOException
        {
            exchange.startBlocking();
            final int length = (int) exchange.getRequestContentLength();
            return exchange.getInputStream().readNBytes(length);
        }

        @Override
        public void readBody(FullBytesCallback callback)
        {
            exchange.getRequestReceiver().receiveFullBytes((httpServerExchange, bytes) -> callback.handle(UndertowExchange.this, bytes));
        }

        @Override
        public void readBody(PartialBytesCallback callback)
        {
            exchange.getRequestReceiver().receivePartialBytes((httpServerExchange, bytes, last) -> callback.handle(UndertowExchange.this, bytes, last));
        }

    }

    private class UndertowSender implements ResponseSender
    {
        @Override
        public OutputStream getOutputStream()
        {
            return exchange.getOutputStream();
        }

        @Override
        public void send(String contentType, byte[] bytes)
        {
            final HeaderMap headerMap = exchange.getResponseHeaders();
            headerMap.add(Headers.CONTENT_TYPE, contentType);
            headerMap.add(Headers.CONTENT_LENGTH, bytes.length);
            exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
        }
    }
}
