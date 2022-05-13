package com.tlc.crm.common.action.res;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.json.Json;
import com.tlc.commons.json.JsonArray;
import com.tlc.crm.common.action.CrmResponse;
import com.tlc.crm.common.internal.resource.CommonErrorCodes;
import com.tlc.i18n.I18nAccess;
import com.tlc.i18n.I18nResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/**
 * <p>
 * Abstract model of CRM response
 * </p>
 *
 * @author Selvakumar
 * @Version 1.0
 */
public abstract class AbstractCrmResponse implements CrmResponse {

    private final I18nResolver i18nResolver;
    private ErrorCode errorCode;
    private Boolean successFlag = true;

    protected AbstractCrmResponse(final Locale locale) {
        Objects.requireNonNull(locale);
        this.i18nResolver = I18nAccess.get().getResolver(locale);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public final ErrorCode getErrorCode() {
        return this.errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String resolveI18nString(String i18nString) {
        return this.i18nResolver.get(i18nString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final JsonArray resolveI18nString(Collection<String> i18nStrings) {
        final JsonArray resolvedStrings = Json.array();

        i18nStrings.forEach(i18nString -> {
            resolvedStrings.put(resolveI18nString(i18nString));
        });

        return resolvedStrings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        try (final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
             final ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayStream)) {
            outputStream.writeObject(this);
            return byteArrayStream.toByteArray();
        } catch (IOException e) {
            throw ErrorCode.get(CommonErrorCodes.ENTITY_TO_BYTE_CONVERSION_FAILED);
        }
    }

}
