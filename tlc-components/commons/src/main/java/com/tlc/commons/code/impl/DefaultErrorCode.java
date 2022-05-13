package com.tlc.commons.code.impl;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodeProvider;

import java.util.Objects;


/**
 * @author Abishek
 * @version 1.0
 */
public class DefaultErrorCode extends ErrorCode
{
    private final ErrorCodeProvider errorCodeProvider;
    public DefaultErrorCode(ErrorCodeProvider errorCodeProvider)
    {
        super(errorCodeProvider.name());
        this.errorCodeProvider= Objects.requireNonNull(errorCodeProvider);
    }

    public DefaultErrorCode(ErrorCodeProvider errorCodeProvider, Throwable cause)
    {
        super(errorCodeProvider.name(), cause);
        this.errorCodeProvider= Objects.requireNonNull(errorCodeProvider);
    }

    public DefaultErrorCode(ErrorCodeProvider errorCodeProvider, String message)
    {
        super(errorCodeProvider.name() +" : "+ message);
        this.errorCodeProvider= Objects.requireNonNull(errorCodeProvider);
    }

    public DefaultErrorCode(ErrorCodeProvider errorCodeProvider, String message, Throwable cause)
    {
        super(errorCodeProvider.name() +" : "+ message, cause);
        this.errorCodeProvider= Objects.requireNonNull(errorCodeProvider);
    }

    @Override
    public long getCode()
    {
        return errorCodeProvider.getCode();
    }

    @Override
    public ErrorCodeProvider getProvider()
    {
        return errorCodeProvider;
    }

    @Override
    public boolean hasStackTrace()
    {
        return true;
    }
}
