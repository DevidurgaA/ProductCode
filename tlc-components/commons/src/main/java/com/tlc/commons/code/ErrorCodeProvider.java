package com.tlc.commons.code;


/**
 * @author Abishek
 * @version 1.0
 */
public interface ErrorCodeProvider
{
    ErrorCodeProvider UNKNOWN = new ErrorCodeProvider()
    {

        @Override
        public String name()
        {
            return "UNKNOWN";
        }

        @Override
        public int getCode()
        {
            return -1;
        }
    };

    String name();

    int getCode();

    default String getMessage()
    {
        return null;
    }

    default String getTroubleshootingLink()
    {
        return null;
    }
}
