package com.tlc.commons.util;

import com.tlc.commons.code.ErrorCode;
import com.tlc.commons.code.ErrorCodes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Abishek
 * @version 1.0
 */
public final class ConfLoader
{
    public static Map<String, String> load(String fileName) throws IOException
    {
        final File file = new File(Env.getConfDirectory(), fileName);
        if(!file.exists())
        {
            throw ErrorCode.get(ErrorCodes.FILE_NOT_FOUND);
        }
        try(FileReader reader = new FileReader(file))
        {
            final Properties properties = new Properties();
            properties.load(reader);
            return properties.entrySet().stream().collect(
                    Collectors.toMap(entry -> String.valueOf(entry.getKey()), entry -> String.valueOf(entry.getValue())));
        }
    }
}
