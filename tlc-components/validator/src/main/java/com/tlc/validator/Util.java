package com.tlc.validator;

import java.util.regex.Pattern;

/**
 * @author Abishek
 * @version 1.0
 */
public class Util
{
    private static final Pattern PATTERN_1 = Pattern.compile("^[^-.][\\p{L}0-9-_.]+[^-.]$");
    private static final Pattern PATTERN_2 = Pattern.compile("^[^-. ][\\p{L}0-9-_.]+[^-. ]$");

    public static boolean isValid_withoutSpace(String chars)
    {
        return PATTERN_1.matcher(chars).matches();
    }

    public static boolean isValid_withSpace(String chars)
    {
        return !chars.isEmpty() && PATTERN_2.matcher(chars).matches();
    }

}
