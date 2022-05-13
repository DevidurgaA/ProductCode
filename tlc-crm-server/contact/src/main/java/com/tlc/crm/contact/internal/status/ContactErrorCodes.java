package com.tlc.crm.contact.internal.status;

import com.tlc.commons.code.ErrorCodeGroup;
import com.tlc.commons.code.ErrorCodeProvider;

/**
 * <p>
 *     Provides the error codes of crm contact module
 * </p>
 *
 * @author Selvakumar
 * @version 1.0
 */
public enum ContactErrorCodes implements ErrorCodeProvider {

    INVALID_REQUEST_DATA(0x01),
    CONTACT_DATA_VALIDATION_FAILED(0x02),
    EMAIL_DATA_VALIDATION_FAILED(0x03),
    MOBILE_DATA_VALIDATION_FAILED(0x04),
    COMPANY_DATA_VALIDATION_FAILED(0x05),
    LIFE_CYCLE_STAGE_DATA_VALIDATION_FAILED(0x06),
    CONTACT_SOURCE_DATA_VALIDATION_FAILED(0x07),


    ENTITY_NOT_FOUND(0x20),
    CONTACT_NOT_FOUND(0x21),
    CONTACT_SOURCE_NOT_FOUND(0x22),
    CONTACT_MOBILE_NOT_FOUND(0x23),
    CONTACT_EMAIL_NOT_FOUND(0x24),
    LIFE_CYCLE_STAGE_NOT_FOUND(0x25),
    COMPANY_NOT_FOUND(0x26),

    CONTACT_CREATE_ACTION_FAILED(0x40),
    CONTACT_EMAIL_CREATE_ACTION_FAILED(0x41),
    CONTACT_MOBILE_CREATE_ACTION_FAILED(0x42),
    COMPANY_CREATE_ACTION_FAILED(0x43),
    CONTACT_SOURCE_CREATE_ACTION_FAILED(0x44),
    LIFE_CYCLE_STAGE_CREATE_ACTION_FAILED(0x45),

    CONTACT_UPDATE_ACTION_FAILED(0x60),
    CONTACT_EMAIL_UPDATE_ACTION_FAILED(0x61),
    CONTACT_MOBILE_UPDATE_ACTION_FAILED(0x62),
    COMPANY_UPDATE_ACTION_FAILED(0x63),
    CONTACT_SOURCE_UPDATE_ACTION_FAILED(0x64),
    LIFE_CYCLE_STAGE_UPDATE_ACTION_FAILED(0x65),
    ;

    private final int code;

    ContactErrorCodes(int localCode) {
        this.code = ContactErrorCodes.ContactErrorCodeGroup.GROUP.getConvertedCode(localCode);
    }

    @Override
    public int getCode()
    {
        return code;
    }

    private static class ContactErrorCodeGroup implements ErrorCodeGroup {
        private static final ErrorCodeGroup GROUP = new ContactErrorCodes.ContactErrorCodeGroup();
        @Override
        public int getPrefix()
        {
            return 0x15_0_0001;
        }
    }
}
