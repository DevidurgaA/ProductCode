package com.tlc.commons.code;

/**
 * @author Abishek
 * @version 1.0
 */

public enum ErrorCodes implements ErrorCodeProvider
{
    NO_ERROR(0x0),
    INVALID_ACCESS(0x1),
    INVALID_FORMAT(0x2),
    INVALID_DATA(0x3),
    INVALID_PARAMETER(0x4),
    ACCESS_DENIED(0x5),
    NOT_SUPPORTED(0x6),
    UNKNOWN_ERROR(0x7),
    REQUEST_FAILED(0x8),
    UNKNOWN_INPUT(0xA),
    ALREADY_INITIALIZED(0xB),
    NOT_INITIALIZED(0xC),
    VERSION_MISMATCH(0xD),
    INVALID_STATE(0xE),
    DATA_OVERFLOW(0xF),
    CONNECTION_NOT_AVAILABLE(0x10),
    FILE_NOT_FOUND(0x11),
    ;

    private final int code;
    ErrorCodes(int localCode)
    {
        this.code = Group.GROUP.getConvertedCode(localCode);
    }

    @Override
    public int getCode()
    {
        return code;
    }

    private static class Group implements ErrorCodeGroup
    {
        private static final ErrorCodeGroup GROUP = new Group();
        @Override
        public int getPrefix()
        {
            return 0;
        }
    }
}
