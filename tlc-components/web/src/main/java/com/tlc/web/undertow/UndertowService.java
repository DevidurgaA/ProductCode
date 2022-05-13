package com.tlc.web.undertow;

import com.tlc.commons.service.Service;
import com.tlc.web.Action;
import com.tlc.web.WebService;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.RoutingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */

public class UndertowService implements WebService, Service
{
    private Undertow server;
    private RoutingHandler routingHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowService.class);

    @Override
    public void start(Map<String, String> input)
    {
        final String port = Objects.requireNonNull(input.get("http.port"));
        this.routingHandler = Handlers.routing();
        this.server = Undertow.builder()
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .addHttpListener(Integer.parseInt(port), "0.0.0.0", routingHandler).build();
        server.start();
        routingHandler.get("/health", exchange ->
        {
            exchange.getResponseSender().send("success");
            exchange.endExchange();
        });
        //TODO set request body max limit
        LOGGER.info("Undertow Service Activated");
    }

    @Override
    public void stop()
    {
        if(server != null)
        {
            server.stop();
        }
    }

    @Override
    public void register(String path, Action action)
    {
        LOGGER.info("Registering web action for path {}", path);
        routingHandler.post(path, exchange ->
        {
            final UndertowExchange uExchange = new UndertowExchange(exchange);
            action.process(uExchange);
            exchange.endExchange();
        });
    }

    @Override
    public void unregister(String path)
    {
        LOGGER.info("Removing web action from path {}", path);
        routingHandler.remove(path);
    }

}
