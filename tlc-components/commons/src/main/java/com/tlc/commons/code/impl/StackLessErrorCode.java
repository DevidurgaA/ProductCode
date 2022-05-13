package com.tlc.commons.code.impl;

import com.tlc.commons.code.ErrorCodeProvider;


/**
 * @author Abishek
 * @version 1.0
 */
public class StackLessErrorCode extends DefaultErrorCode
{
    public StackLessErrorCode(ErrorCodeProvider errorCodeProvider)
    {
        super(errorCodeProvider);
    }

    public StackLessErrorCode(ErrorCodeProvider errorCodeProvider, Throwable cause)
    {
        super(errorCodeProvider, cause);
    }

    public StackLessErrorCode(ErrorCodeProvider errorCodeProvider, String message)
    {
        super(errorCodeProvider, message);
    }

    public StackLessErrorCode(ErrorCodeProvider errorCodeProvider, String message, Throwable cause)
    {
        super(errorCodeProvider, message, cause);
    }

    @Override
    public boolean hasStackTrace()
    {
        return getCause() != null;
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
