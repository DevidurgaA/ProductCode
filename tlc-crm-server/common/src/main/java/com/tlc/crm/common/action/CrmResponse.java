package com.tlc.crm.common.action;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.json.JsonArray;

import java.util.Collection;

/**
 * <p>
 * HTTP response wrapper
 * </p>
 *
 * @author Selvakumar
 */
public interface CrmResponse {

    /**
     * <p>
     *     Returns the content type of the CRM response
     * </p>
     * @return
     */
    String getContentType();

    /**
     * <p>
     * Puts boolean value on the specified key
     * </p>
     *
     * @param key
     * @param value
     */
    void put(String key, Object value);

    /**
     * <p>
     * Resolves i18nString and returns the proper value based on the locale.
     * If no keys matches the specified i18nString, it will return the i18nString
     * </p>
     *
     * @param i18nString
     * @return
     */
    String resolveI18nString(String i18nString);

    /**
     * <p>
     * Resolves i18nStrings and returns the resolved values based on the locale.
     * If no keys matches an specified i18nStrings, it will just set the unresolved string in the JSON array
     * </p>
     *
     * @param i18nStrings
     * @return
     */
    JsonArray resolveI18nString(Collection<String> i18nStrings);

    /**
     * Sets the response code
     *
     * @param code
     */
    void setErrorCode(final ErrorCode code);

    /**
     * <p>
     *     Returns the response data in bytes
     * </p>
     * @return
     */
    byte[] getBytes();

}
