package com.tlc.crm.common.action.res;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;
import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonObject;

import java.util.Locale;

/**
 * <p>
 * Http JSON response holder
 * </p>
 *
 * @author Selvakumar
 */
public class CrmJsonResponse extends AbstractCrmResponse {

    private static final String CONTENT_TYPE = "application/json";

    private final JsonObject response;

    public CrmJsonResponse() {
        this(Locale.getDefault());
    }

    public CrmJsonResponse(Locale locale) {
        super(locale);
        this.response = Json.object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getContentType() {
        return CrmJsonResponse.CONTENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(String key, Object value) {
        this.response.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] getBytes() {
        final JsonObject jsonObject = Json.object();
        final ErrorCode errorCode = getErrorCode();

        jsonObject.put("code", null == errorCode ? ErrorCodes.NO_ERROR.getCode() : errorCode.getCode());
        jsonObject.put("data", this.response);

        return jsonObject.getBytes();
    }

}
