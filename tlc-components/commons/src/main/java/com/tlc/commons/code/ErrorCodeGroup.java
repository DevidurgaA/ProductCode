package com.tlc.commons.code;


/**
 * @author Abishek
 * @version 1.0
 */
public interface ErrorCodeGroup
{
    /*
        Format

        Binary - 12(Group) - 4(Reserved) - 16(Code)

        Hex - 3(Group) - 1(Reserved) - 4(Code)

     */

//    CODE_LIMIT = 0xFFF


    default int getConvertedCode(int code)
    {
        final int index = getPrefix();
        if(code <= 0xFFFF)
        {
            return index + code;
        }
        else
        {
            throw ErrorCode.get(ErrorCodeProvider.UNKNOWN);
        }
    }

    int getPrefix();
}
