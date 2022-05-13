package com.tlc.crm.common.internal.resource;

import com.tlc.commons.code.ErrorCodeGroup;
import com.tlc.commons.code.ErrorCodeProvider;

/**
 * @author Selvakumar
 */
public enum CommonErrorCodes implements ErrorCodeProvider {

    DATA_VALIDATION_FAILED(0x00),
    CREATE_ACTION_DATA_VALIDATION_FAILED(0x01),
    UPDATE_ACTION_DATA_VALIDATION_FAILED(0x02),
    DELETE_ACTION_DATA_VALIDATION_FAILED(0x03),

    ENTITY_NOT_FOUND(0x20),
    ENTITY_TO_BYTE_CONVERSION_FAILED(0x22),
    CREATE_ACTION_FAILED(0x23),
    UPDATE_ACTION_FAILED(0x24),
    DELETE_ACTION_FAILED(0x25),
    PARTIAL_UPDATE_ACTION_FAILED(0x26),
    BULK_UPDATE_ACTION_FAILED(0x27),
    ;
    private final int code;

    CommonErrorCodes(int localCode) {
        this.code = CommonErrorCodes.CommonErrorCodesGroup.GROUP.getConvertedCode(localCode);
    }

    @Override
    public int getCode()
    {
        return code;
    }

    private static class CommonErrorCodesGroup implements ErrorCodeGroup {
        private static final ErrorCodeGroup GROUP = new CommonErrorCodes.CommonErrorCodesGroup();
        @Override
        public int getPrefix()
        {
            return 0x10_0_0001;
        }
    }
}
