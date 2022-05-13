package com.tlc.crm.common.action;

import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;
import com.tlc.i18n.I18nAccess;
import com.tlc.i18n.I18nResolver;
import com.tlc.web.WebExchange;

import java.net.HttpCookie;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class CrmRequest {

    private final byte[] message;
    private final WebExchange exchange;
    private final JsonObject body;
    private final Long orgId;
    private final Long userId; //TODO
    private final I18nResolver i18nResolver; //TODO

    CrmRequest(WebExchange exchange, byte[] message) {
        this.exchange = Objects.requireNonNull(exchange);
        this.userId = 1L;
        this.i18nResolver = I18nAccess.get().getResolver(Locale.getDefault());
        this.message = message;

        final String header = header("Content-Type");

        if (null != header && "application/json".equalsIgnoreCase(header)) {
            this.body = message.length == 0 ? Json.object() : Json.object(message);
        } else {
            this.body = Json.object();
        }

        this.orgId = this.body.optLong("orgId", 1);
    }

    public JsonObject getRequestJson() {
        return body;
    }

    public String parameter(final String name) {
        return exchange.param(name).findAny().orElse(null);
    }

    public String header(final String name) {
        return exchange.header(name).findAny().orElse(null);
    }

    public void header(String name, String value) {
        exchange.header(name, value);
    }

    HttpCookie cookie(String name) {
        return exchange.cookie(name);
    }

    void cookie(HttpCookie cookie) {
        exchange.cookie(cookie);
    }

    public I18nResolver i18nResolver() {
        return i18nResolver;
    }

    public Long orgId() {
        return orgId;
    }

    public Long userId() {
        return userId;
    }

}
