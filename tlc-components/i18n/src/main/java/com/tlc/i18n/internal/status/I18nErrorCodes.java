package com.tlc.i18n.internal.status;

import com.tlc.commons.code.ErrorCodeGroup;
import com.tlc.commons.code.ErrorCodeProvider;

public enum I18nErrorCodes implements ErrorCodeProvider
{
    I18N_PROPERTY_FILE_READ_ACTION_FAILED(0x01),
    I18N_INVALID_LOCALE_FORMAT(0x02),
    I18N_WORK_DIRECTORY_CREATION_FAILED(0x03),
    I18N_PROPERTY_FILE_COPY_ACTION_FAILED(0x04),
    I18N_PROPERTY_FILE_HISTORY_RECORD_NOT_FOUND(0x05),
    I18N_PROPERTY_FILE_HISTORY_RECORD_READ_ACTION_FAILED(0x06),
    I18N_PROPERTY_FILE_HISTORY_RECORD_UPDATE_FAILED(0x07),
    I18N_OLD_PROPERTY_FILE_NOT_FOUND(0x08),
    I18N_OLD_PROPERTY_READ_ACTION_FAILED(0x09),
    ;

    private final int code;
    I18nErrorCodes(int localCode)
    {
        this.code = I18nErrorCodeGroup.GROUP.getConvertedCode(localCode);
    }

    @Override
    public int getCode()
    {
        return code;
    }

    private static class I18nErrorCodeGroup implements ErrorCodeGroup
    {
        private static final ErrorCodeGroup GROUP = new I18nErrorCodeGroup();
        @Override
        public int getPrefix()
        {
            return 0x10_0_0001;
        }
    }
}

